package tn.iteam.backend.service.impl;

import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.LeaveRequest;
import tn.iteam.backend.entity.LeaveStatus;
import tn.iteam.backend.entity.LeaveType;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.service.LeaveRequestService;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;

    public LeaveRequestServiceImpl(
            LeaveRequestRepository leaveRequestRepository,
            EmployeeProfileRepository employeeProfileRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper
    ) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public List<LeaveRequest> findAll() {
        return leaveRequestRepository.findAll();
    }

    @Override
    public LeaveRequest findById(Long id) {
        return leaveRequestRepository.findById(id).orElseThrow(() -> new BusinessException("Leave request not found"));
    }

    @Override
    public List<LeaveRequest> findMine() {
        User me = currentUserProvider.requireCurrentUser();
        return leaveRequestRepository.findByEmployeeId(me.getId());
    }

    @Override
    public List<LeaveRequest> findForUser(Long userId) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.HR && rn != RoleName.MANAGER && rn != RoleName.ADMIN) {
            throw new BusinessException("Not allowed to view leave history for other users");
        }
        return leaveRequestRepository.findByEmployeeId(userId);
    }

    @Override
    public LeaveRequest request(LeaveRequest leaveRequest) {
        User me = currentUserProvider.requireCurrentUser();

        if (leaveRequest.getId() != null) leaveRequest.setId(null);
        leaveRequest.setEmployee(me);
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
        if (days <= 0) throw new BusinessException("Invalid days");
        leaveRequest.setDays(days);

        EmployeeProfile profile = employeeProfileRepository.findByUserId(me.getId())
                .orElseThrow(() -> new BusinessException("Employee profile not found"));

        int remaining = profile.getRemainingLeaveDays() == null ? 30 : profile.getRemainingLeaveDays();
        if (days > remaining) {
            throw new BusinessException("Leave days exceeded. Remaining: " + remaining);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        if (saved.getManager() != null) {
            notificationHelper.notify(
                    saved.getManager(),
                    NotificationType.LEAVE_REQUESTED,
                    "Leave requested by " + me.getFullName() + " (" + days + " days)"
            );
        }

        return saved;
    }

    @Override
    public LeaveRequest approve(Long id) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.MANAGER && rn != RoleName.HR) {
            throw new BusinessException("Only MANAGER or HR can approve leaves");
        }

        LeaveRequest lr = findById(id);
        lr.setStatus(LeaveStatus.APPROVED);

        EmployeeProfile profile = employeeProfileRepository.findByUserId(lr.getEmployee().getId())
                .orElseThrow(() -> new BusinessException("Employee profile not found"));

        int remaining = profile.getRemainingLeaveDays() == null ? 30 : profile.getRemainingLeaveDays();
        int newRemaining = remaining - (lr.getDays() == null ? 0 : lr.getDays());
        if (newRemaining < 0) {
            throw new BusinessException("Cannot approve, leave days would be exceeded");
        }
        profile.setRemainingLeaveDays(newRemaining);
        employeeProfileRepository.save(profile);

        LeaveRequest saved = leaveRequestRepository.save(lr);

        notificationHelper.notify(
                saved.getEmployee(),
                NotificationType.LEAVE_APPROVED,
                "Your leave was approved (" + saved.getDays() + " days)"
        );

        return saved;
    }

    @Override
    public LeaveRequest reject(Long id) {
        User me = currentUserProvider.requireCurrentUser();
        RoleName rn = me.getRole() == null ? null : me.getRole().getName();
        if (rn != RoleName.MANAGER && rn != RoleName.HR) {
            throw new BusinessException("Only MANAGER or HR can reject leaves");
        }

        LeaveRequest lr = findById(id);
        lr.setStatus(LeaveStatus.REJECTED);

        LeaveRequest saved = leaveRequestRepository.save(lr);

        notificationHelper.notify(
                saved.getEmployee(),
                NotificationType.LEAVE_REJECTED,
                "Your leave was rejected"
        );

        return saved;
    }

    @Override
    public void delete(Long id) {
        leaveRequestRepository.deleteById(id);
    }
}

