package tn.iteam.common.events;

import java.time.Instant;

public record NotificationEvent(
        String type,
        Long userId,
        String title,
        String message,
        Instant occurredAt
) {}
