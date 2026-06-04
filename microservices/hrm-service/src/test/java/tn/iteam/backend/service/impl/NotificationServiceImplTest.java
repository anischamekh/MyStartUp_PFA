package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Notification;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.NotificationRepository;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void findMine_returnsUserNotifications() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(3L, "u", "EMPLOYEE", "U"));
        Notification n = new Notification();
        n.setRecipientUserId(3L);
        when(notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(n));

        assertEquals(1, notificationService.findMine().size());
    }

    @Test
    void markRead_ownNotification() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(3L, "u", "EMPLOYEE", "U"));
        Notification n = new Notification();
        n.setId(1L);
        n.setRecipientUserId(3L);
        n.setRead(false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(n)).thenReturn(n);

        Notification updated = notificationService.markRead(1L);
        assertTrue(updated.getRead());
    }

    @Test
    void markRead_deniedForOtherUser() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "a", "EMPLOYEE", "A"));
        Notification n = new Notification();
        n.setRecipientUserId(99L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThrows(BusinessException.class, () -> notificationService.markRead(1L));
    }

    @Test
    void delete_ownNotification() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(3L, "u", "EMPLOYEE", "U"));
        Notification n = new Notification();
        n.setRecipientUserId(3L);
        when(notificationRepository.findById(2L)).thenReturn(Optional.of(n));

        notificationService.delete(2L);
        verify(notificationRepository).delete(n);
    }
}
