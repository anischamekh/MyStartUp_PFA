package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.config.FileStorageProperties;
import tn.iteam.backend.entity.EmployeeDocument;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeDocumentRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class EmployeeDocumentServiceImplTest {

    @Mock
    private EmployeeDocumentRepository documentRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private FileStorageProperties storageProperties;
    @Mock
    private UserSnapshotService userSnapshotService;

    @InjectMocks
    private EmployeeDocumentServiceImpl documentService;

    @Test
    void findAllForCurrentUserOrHr_employeeSeesOwn() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(5L, "e", "EMPLOYEE", "E"));
        EmployeeDocument doc = new EmployeeDocument();
        doc.setUserId(5L);
        when(documentRepository.findByUserIdOrderByUploadDateDesc(5L)).thenReturn(List.of(doc));
        when(userSnapshotService.findById(5L)).thenReturn(java.util.Optional.empty());
        assertEquals(1, documentService.findAllForCurrentUserOrHr().size());
    }

    @Test
    void findForUser_deniedForOtherEmployee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        assertThrows(BusinessException.class, () -> documentService.findForUser(99L));
    }
}
