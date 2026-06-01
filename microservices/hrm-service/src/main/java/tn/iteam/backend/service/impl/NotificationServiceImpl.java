package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Notification;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.backend.service.NotificationService;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserProvider currentUserProvider;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.notificationRepository = notificationRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public List<Notification> findMine() {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(me.userId());
    }

    @Override
    public Notification markRead(Long id) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new BusinessException("Notification not found"));
        if (!me.userId().equals(n.getRecipientUserId())) {
            throw new BusinessException("Not allowed");
        }
        n.setRead(true);
        return notificationRepository.save(n);
    }

    @Override
    public void delete(Long id) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new BusinessException("Notification not found"));
        if (!me.userId().equals(n.getRecipientUserId())) {
            throw new BusinessException("Not allowed");
        }
        notificationRepository.delete(n);
    }
}
