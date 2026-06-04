package tn.iteam.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import tn.iteam.backend.entity.EmployeeHrData;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.repository.EmployeeHrDataRepository;
import tn.iteam.backend.repository.UserSnapshotRepository;
import tn.iteam.common.dto.UserSummaryDto;
import tn.iteam.common.events.EmployeeUpdatedEvent;

@ExtendWith(MockitoExtension.class)
class UserSnapshotServiceTest {

    @Mock
    private UserSnapshotRepository userSnapshotRepository;
    @Mock
    private EmployeeHrDataRepository employeeHrDataRepository;
    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private UserSnapshotService userSnapshotService;

    @Test
    void mapByIds_empty() {
        assertTrue(userSnapshotService.mapByIds(List.of()).isEmpty());
    }

    @Test
    void applyEmployeeUpdated_persistsSnapshotAndHrData() {
        when(userSnapshotRepository.findById(7L)).thenReturn(Optional.empty());
        when(employeeHrDataRepository.findById(7L)).thenReturn(Optional.empty());

        userSnapshotService.applyEmployeeUpdated(new EmployeeUpdatedEvent(
                7L, "u", "User", "u@test.com", "EMPLOYEE", 1L, "T", 10, "UPDATED", Instant.now()));

        verify(userSnapshotRepository).save(any(UserSnapshot.class));
        verify(employeeHrDataRepository).save(any(EmployeeHrData.class));
    }

    @Test
    void requireById_syncsFromAuthWhenMissing() {
        when(userSnapshotRepository.findById(12L)).thenReturn(Optional.empty(), Optional.of(snapshot(12L)));
        when(authServiceClient.getUser(12L)).thenReturn(new UserSummaryDto(
                12L, "sync", "Sync User", "s@test.com", "EMPLOYEE", 2L, "Team"));

        UserSnapshot result = userSnapshotService.requireById(12L);
        assertEquals(12L, result.getId());
        verify(userSnapshotRepository).save(any(UserSnapshot.class));
    }

    @Test
    void deleteUser_removesHrDataAndSnapshot() {
        userSnapshotService.deleteUser(3L);
        verify(employeeHrDataRepository).deleteById(3L);
        verify(userSnapshotRepository).deleteById(3L);
    }

    private static UserSnapshot snapshot(Long id) {
        UserSnapshot s = new UserSnapshot();
        s.setId(id);
        return s;
    }
}
