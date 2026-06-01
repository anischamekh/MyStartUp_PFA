package tn.iteam.common.events;

import java.time.Instant;

public record ProjectCreatedEvent(
        Long projectId,
        String projectName,
        Long createdById,
        Instant occurredAt
) {}
