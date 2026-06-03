package tn.iteam.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.repository.UserSnapshotRepository;
import tn.iteam.common.dto.UserSummaryDto;
import tn.iteam.common.events.EmployeeUpdatedEvent;

@ExtendWith(MockitoExtension.class)
class UserSnapshotServiceTest {

    @Mock
    private UserSnapshotRepository userSnapshotRepository;
    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private UserSnapshotService userSnapshotService;

    @Test
    void mapByIds_empty() {
        assertTrue(userSnapshotService.mapByIds(List.of()).isEmpty());
    }

    @Test
    void applyEmployeeUpdated_persistsSnapshot() {
        when(userSnapshotRepository.findById(7L)).thenReturn(Optional.empty());
        userSnapshotService.applyEmployeeUpdated(new EmployeeUpdatedEvent(
                7L, "u", "User", "u@test.com", "EMPLOYEE", 1L, "T", 10, "UPDATED", Instant.now()));
        verify(userSnapshotRepository).save(org.mockito.ArgumentMatchers.any(UserSnapshot.class));
    }

    @Test
    void deleteUser() {
        userSnapshotService.deleteUser(3L);
        verify(userSnapshotRepository).deleteById(3L);
    }

    @Test
    void findById() {
        UserSnapshot snap = new UserSnapshot();
        snap.setId(1L);
        when(userSnapshotRepository.findById(1L)).thenReturn(Optional.of(snap));
        assertEquals(1L, userSnapshotService.findById(1L).orElseThrow().getId());
    }
}
