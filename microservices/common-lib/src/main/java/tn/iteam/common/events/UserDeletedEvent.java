package tn.iteam.common.events;

import java.time.Instant;

public record UserDeletedEvent(Long userId, Instant occurredAt) {}
