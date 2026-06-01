package tn.iteam.backend.service.impl;

import java.time.Instant;
import org.springframework.stereotype.Component;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.common.events.NotificationEvent;

@Component
public class NotificationHelper {

    private final EventPublisher eventPublisher;

    public NotificationHelper(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void notify(Long recipientUserId, NotificationType type, String message) {
        if (recipientUserId == null) {
            return;
        }
        eventPublisher.publishNotification(new NotificationEvent(
                type.name(),
                recipientUserId,
                type.name(),
                message,
                Instant.now()
        ));
    }
}
