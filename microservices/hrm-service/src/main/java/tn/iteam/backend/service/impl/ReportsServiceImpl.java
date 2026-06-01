package tn.iteam.backend.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.dto.ReportsSummaryDto;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.LeaveRequestRepository;
import tn.iteam.backend.repository.UserSnapshotRepository;
import tn.iteam.backend.service.ReportsService;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional(readOnly = true)
public class ReportsServiceImpl implements ReportsService {

    private final UserSnapshotRepository userSnapshotRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReportsServiceImpl(
            UserSnapshotRepository userSnapshotRepository,
            LeaveRequestRepository leaveRequestRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.userSnapshotRepository = userSnapshotRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public ReportsSummaryDto summary() {
        requireReader();
        ReportsSummaryDto dto = new ReportsSummaryDto();
        dto.setTasksByStatus(Map.of());
        Map<String, Long> byTeam = new LinkedHashMap<>();
        for (UserSnapshot snapshot : userSnapshotRepository.findAll()) {
            String key = snapshot.getTeamName() == null ? "No team" : snapshot.getTeamName();
            byTeam.merge(key, 1L, Long::sum);
        }
        dto.setEmployeesByTeam(byTeam);
        dto.setLeavesByStatus(
                leaveRequestRepository.findAll().stream()
                        .collect(Collectors.groupingBy(
                                l -> l.getStatus() == null ? "UNKNOWN" : l.getStatus().name(),
                                LinkedHashMap::new,
                                Collectors.counting()
                        ))
        );
        return dto;
    }

    @Override
    public byte[] exportSummaryPdf() {
        return ("MyStartUp HRM Report\n" + summary()).getBytes();
    }

    @Override
    public byte[] exportSummaryExcel() {
        return ("MyStartUp HRM Report").getBytes();
    }

    private void requireReader() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!java.util.List.of("MANAGER", "HR", "ADMIN").contains(me.role())) {
            throw new BusinessException("Not allowed to view reports");
        }
    }
}
