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
import tn.iteam.backend.messaging.EventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationHelperTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Test
    void notify_publishesEvent() {
        notificationHelper.notify(1L, NotificationType.TASK_ASSIGNED, "Assigned");
        verify(eventPublisher).publishNotification(any());
    }

    @Test
    void notify_skipsNullRecipient() {
        notificationHelper.notify(null, NotificationType.TASK_ASSIGNED, "x");
        verify(eventPublisher, never()).publishNotification(any());
    }
}
