package tn.iteam.backend.service.impl;

import org.springframework.stereotype.Component;
import tn.iteam.backend.entity.Notification;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.repository.NotificationRepository;

@Component
public class NotificationHelper {

    private final NotificationRepository notificationRepository;

    public NotificationHelper(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notify(Long recipientUserId, NotificationType type, String message) {
        if (recipientUserId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notificationRepository.save(notification);
    }
}
