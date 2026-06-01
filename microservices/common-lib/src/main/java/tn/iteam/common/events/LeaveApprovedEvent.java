package tn.iteam.common.events;

import java.time.Instant;
import java.time.LocalDate;

public record LeaveApprovedEvent(
        Long leaveId,
        Long employeeId,
        Long managerId,
        LocalDate startDate,
        LocalDate endDate,
        Integer days,
        Instant occurredAt
) {}
