package tn.iteam.backend.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class UserDeletionCleanupServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserSnapshotService userSnapshotService;

    @InjectMocks
    private UserDeletionCleanupService cleanupService;

    @Test
    void cleanup_clearsAssignmentsAndManager() {
        Project managed = new Project();
        managed.setId(1L);
        managed.setManagerUserId(5L);
        when(projectRepository.findByManagerUserId(5L)).thenReturn(List.of(managed));

        cleanupService.cleanup(5L);

        verify(taskRepository).clearAssignedToForUser(5L);
        verify(taskRepository).clearCreatedByForUser(5L);
        verify(projectRepository).save(managed);
        verify(userSnapshotService).deleteUser(5L);
    }
}
