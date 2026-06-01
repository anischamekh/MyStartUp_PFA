package tn.iteam.common.events;

import java.time.Instant;

public record EmployeeUpdatedEvent(
        Long userId,
        String username,
        String fullName,
        String email,
        String role,
        Long teamId,
        String teamName,
        Integer remainingLeaveDays,
        String eventType,
        Instant occurredAt
) {}
