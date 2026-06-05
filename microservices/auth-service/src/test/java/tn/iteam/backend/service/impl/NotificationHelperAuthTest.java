package tn.iteam.backend.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationHelperAuthTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Test
    void notify_persistsForUser() {
        User user = new User();
        user.setId(1L);
        notificationHelper.notify(user, NotificationType.TASK_ASSIGNED, "Assigned");
        verify(notificationRepository).save(any());
    }

    @Test
    void notify_skipsNullUser() {
        notificationHelper.notify(null, NotificationType.TASK_ASSIGNED, "x");
        verify(notificationRepository, never()).save(any());
    }
}
