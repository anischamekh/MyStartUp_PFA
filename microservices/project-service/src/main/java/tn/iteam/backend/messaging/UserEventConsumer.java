package tn.iteam.backend.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import tn.iteam.backend.service.UserDeletionCleanupService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.events.EmployeeUpdatedEvent;
import tn.iteam.common.events.KafkaTopics;
import tn.iteam.common.events.UserDeletedEvent;

@Component
public class UserEventConsumer {

    private final UserSnapshotService userSnapshotService;
    private final UserDeletionCleanupService userDeletionCleanupService;

    public UserEventConsumer(
            UserSnapshotService userSnapshotService,
            UserDeletionCleanupService userDeletionCleanupService
    ) {
        this.userSnapshotService = userSnapshotService;
        this.userDeletionCleanupService = userDeletionCleanupService;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @KafkaListener(topics = KafkaTopics.USER_EVENTS, groupId = "project-user-sync")
    public void onUserEvent(Object payload) {
        if (payload instanceof EmployeeUpdatedEvent event) {
            userSnapshotService.applyEmployeeUpdated(event);
        } else if (payload instanceof UserDeletedEvent event) {
            userDeletionCleanupService.cleanup(event.userId());
        }
    }
}
