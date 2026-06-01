package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Notification;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.backend.service.NotificationService;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;

    public NotificationServiceImpl(NotificationRepository notificationRepository, CurrentUserProvider currentUserProvider) {
        this.notificationRepository = notificationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<Notification> findMine() {
        User me = currentUserProvider.requireCurrentUser();
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(me.getId());
    }

    @Override
    public Notification markRead(Long id) {
        User me = currentUserProvider.requireCurrentUser();
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        if (n.getRecipient() == null || n.getRecipient().getId() == null || !n.getRecipient().getId().equals(me.getId())) {
            throw new RuntimeException("Not allowed");
        }
        n.setRead(true);
        return notificationRepository.save(n);
    }

    @Override
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }
}

