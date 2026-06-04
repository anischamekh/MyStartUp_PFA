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
import tn.iteam.backend.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationHelperTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Test
    void notify_persistsWhenRecipientPresent() {
        notificationHelper.notify(5L, NotificationType.TASK_ASSIGNED, "Hello");
        verify(notificationRepository).save(any());
    }

    @Test
    void notify_skipsNullRecipient() {
        notificationHelper.notify(null, NotificationType.TASK_ASSIGNED, "Hello");
        verify(notificationRepository, never()).save(any());
    }
}
