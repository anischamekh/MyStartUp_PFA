package tn.iteam.backend.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeHrData;
import tn.iteam.backend.entity.LeaveRequest;
import tn.iteam.backend.entity.LeaveStatus;
import tn.iteam.backend.entity.LeaveType;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.EmployeeHrDataRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.service.LeaveRequestService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.events.LeaveApprovedEvent;
import tn.iteam.common.events.NotificationEvent;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeHrDataRepository employeeHrDataRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;
    private final EventPublisher eventPublisher;
    private final UserSnapshotService userSnapshotService;

    public LeaveRequestServiceImpl(
            LeaveRequestRepository leaveRequestRepository,
            EmployeeHrDataRepository employeeHrDataRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper,
            EventPublisher eventPublisher,
            UserSnapshotService userSnapshotService
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeHrDataRepository = employeeHrDataRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
        this.eventPublisher = eventPublisher;
        this.userSnapshotService = userSnapshotService;
    }

    @Override
    public List<LeaveRequest> findAll() {
        return enrichAll(leaveRequestRepository.findAll());
    }

    @Override
    public LeaveRequest findById(Long id) {
        return enrich(leaveRequestRepository.findById(id).orElseThrow(() -> new BusinessException("Leave request not found")));
    }

    @Override
    public List<LeaveRequest> findMine() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        return enrichAll(leaveRequestRepository.findByEmployeeId(me.userId()));
    }

    @Override
    public List<LeaveRequest> findForUser(Long userId) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("HR", "MANAGER", "ADMIN").contains(me.role())) {
            throw new BusinessException("Not allowed to view leave history for other users");
        }
        return enrichAll(leaveRequestRepository.findByEmployeeId(userId));
    }

    @Override
    public LeaveRequest request(LeaveRequest leaveRequest) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();

        if (leaveRequest.getId() != null) {
            leaveRequest.setId(null);
        }
        leaveRequest.setEmployeeId(me.userId());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        if (leaveRequest.getLeaveType() == null) {
            leaveRequest.setLeaveType(LeaveType.ANNUAL);
        }

        if (leaveRequest.getStartDate() == null || leaveRequest.getEndDate() == null) {
            throw new BusinessException("startDate and endDate are required");
        }
        if (leaveRequest.getEndDate().isBefore(leaveRequest.getStartDate())) {
            throw new BusinessException("endDate must be after startDate");
        }

        int days = (int) ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        if (days <= 0) {
            throw new BusinessException("Invalid days");
        }
        leaveRequest.setDays(days);

        EmployeeHrData hrData = employeeHrDataRepository.findById(me.userId())
                .orElseGet(() -> {
                    EmployeeHrData created = new EmployeeHrData();
                    created.setUserId(me.userId());
                    created.setRemainingLeaveDays(30);
                    return employeeHrDataRepository.save(created);
                });

        int remaining = hrData.getRemainingLeaveDays() == null ? 30 : hrData.getRemainingLeaveDays();
        if (days > remaining) {
            throw new BusinessException("Leave days exceeded. Remaining: " + remaining);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        if (saved.getManagerId() != null) {
            UserSnapshot manager = userSnapshotService.findById(saved.getManagerId()).orElse(null);
            String name = manager == null ? "employee" : manager.getFullName();
            notificationHelper.notify(
                    saved.getManagerId(),
                    NotificationType.LEAVE_REQUESTED,
                    "Leave requested by " + name + " (" + days + " days)"
            );
        }

        return enrich(saved);
    }

    @Override
    public LeaveRequest approve(Long id) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("MANAGER", "HR").contains(me.role())) {
            throw new BusinessException("Only MANAGER or HR can approve leaves");
        }

        LeaveRequest lr = findById(id);
        lr.setStatus(LeaveStatus.APPROVED);

        EmployeeHrData hrData = employeeHrDataRepository.findById(lr.getEmployeeId())
                .orElseThrow(() -> new BusinessException("Employee HR data not found"));

        int remaining = hrData.getRemainingLeaveDays() == null ? 30 : hrData.getRemainingLeaveDays();
        int newRemaining = remaining - (lr.getDays() == null ? 0 : lr.getDays());
        if (newRemaining < 0) {
            throw new BusinessException("Cannot approve, leave days would be exceeded");
        }
        hrData.setRemainingLeaveDays(newRemaining);
        employeeHrDataRepository.save(hrData);

        LeaveRequest saved = leaveRequestRepository.save(lr);

        notificationHelper.notify(
                saved.getEmployeeId(),
                NotificationType.LEAVE_APPROVED,
                "Your leave was approved (" + saved.getDays() + " days)"
        );

        eventPublisher.publishLeaveApproved(new LeaveApprovedEvent(
                saved.getId(),
                saved.getEmployeeId(),
                me.userId(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getDays(),
                Instant.now()
        ));
        eventPublisher.publishNotification(new NotificationEvent(
                NotificationType.LEAVE_APPROVED.name(),
                saved.getEmployeeId(),
                "Leave approved",
                "Your leave was approved (" + saved.getDays() + " days)",
                Instant.now()
        ));

        return enrich(saved);
    }

    @Override
    public LeaveRequest reject(Long id) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!List.of("MANAGER", "HR").contains(me.role())) {
            throw new BusinessException("Only MANAGER or HR can reject leaves");
        }

        LeaveRequest lr = findById(id);
        lr.setStatus(LeaveStatus.REJECTED);
        LeaveRequest saved = leaveRequestRepository.save(lr);

        notificationHelper.notify(
                saved.getEmployeeId(),
                NotificationType.LEAVE_REJECTED,
                "Your leave was rejected"
        );

        return enrich(saved);
    }

    @Override
    public void delete(Long id) {
        leaveRequestRepository.deleteById(id);
    }

    private List<LeaveRequest> enrichAll(List<LeaveRequest> items) {
        List<Long> userIds = items.stream()
                .flatMap(l -> java.util.stream.Stream.of(l.getEmployeeId(), l.getManagerId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserSnapshot> snapshots = userSnapshotService.mapByIds(userIds);
        return items.stream().map(l -> enrich(l, snapshots)).toList();
    }

    private LeaveRequest enrich(LeaveRequest leave) {
        Map<Long, UserSnapshot> snapshots = userSnapshotService.mapByIds(
                java.util.stream.Stream.of(leave.getEmployeeId(), leave.getManagerId())
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList()
        );
        return enrich(leave, snapshots);
    }

    private LeaveRequest enrich(LeaveRequest leave, Map<Long, UserSnapshot> snapshots) {
        leave.enrichEmployee(snapshots.get(leave.getEmployeeId()));
        if (leave.getManagerId() != null) {
            leave.enrichManager(snapshots.get(leave.getManagerId()));
        }
        return leave;
    }
}
