package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Payroll;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.PayrollRepository;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserSnapshotService userSnapshotService;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    @Test
    void findVisible_employeeSeesOwnRows() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(5L, "e", "E", "EMPLOYEE"));
        Payroll row = new Payroll();
        row.setUserId(5L);
        when(payrollRepository.findByUserId(5L)).thenReturn(List.of(row));
        when(userSnapshotService.findById(5L)).thenReturn(Optional.empty());
        assertEquals(1, payrollService.findVisible().size());
    }

    @Test
    void findForUser_deniedForOtherEmployee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "a", "A", "EMPLOYEE"));
        assertThrows(BusinessException.class, () -> payrollService.findForUser(99L));
    }
}
