package tn.iteam.backend.service.impl;

import org.springframework.stereotype.Component;
import tn.iteam.backend.entity.Notification;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.repository.NotificationRepository;

@Component
public class NotificationHelper {

    private final NotificationRepository notificationRepository;

    public NotificationHelper(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notify(User recipient, NotificationType type, String message) {
        if (recipient == null) return;
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setMessage(message);
        n.setRead(false);
        notificationRepository.save(n);
    }
}

