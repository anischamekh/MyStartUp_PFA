package tn.iteam.backend.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

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
    @KafkaListener(topics = KafkaTopics.USER_EVENTS, groupId = "hrm-user-sync")
    public void onUserEvent(Object payload) {
        if (payload instanceof EmployeeUpdatedEvent event) {
            userSnapshotService.applyEmployeeUpdated(event);
            log.info("Synced user snapshot userId={}", event.userId());
        } else if (payload instanceof UserDeletedEvent event) {
            userDeletionCleanupService.cleanup(event.userId());
            log.info("Cleaned HRM data for deleted userId={}", event.userId());
        }
    }
}
