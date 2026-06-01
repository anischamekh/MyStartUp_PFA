package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.Notification;

public interface NotificationService {
    List<Notification> findMine();
    Notification markRead(Long id);
    void delete(Long id);
}

