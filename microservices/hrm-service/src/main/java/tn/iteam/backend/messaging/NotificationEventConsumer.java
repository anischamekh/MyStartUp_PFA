package tn.iteam.backend.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import tn.iteam.backend.entity.Notification;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.common.events.KafkaTopics;
import tn.iteam.common.events.NotificationEvent;

@Component
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;

    public NotificationEventConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EVENTS, groupId = "hrm-notification-writer")
    public void onNotification(NotificationEvent event) {
        Notification notification = new Notification();
        notification.setRecipientUserId(event.userId());
        notification.setType(parseType(event.type()));
        notification.setMessage(event.message());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    private NotificationType parseType(String type) {
        try {
            return NotificationType.valueOf(type);
        } catch (Exception ex) {
            return NotificationType.LEAVE_APPROVED;
        }
    }
}
