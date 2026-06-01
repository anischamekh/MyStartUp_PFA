package tn.iteam.common.events;

import java.time.Instant;

public record TaskValidatedEvent(
        Long taskId,
        Long assigneeId,
        Long validatorId,
        Integer validatedProgressFloor,
        Instant occurredAt
) {}
