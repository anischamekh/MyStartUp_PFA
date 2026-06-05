package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Payroll;
import tn.iteam.backend.entity.UserSnapshot;
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
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(5L, "e", "EMPLOYEE", "E"));
        Payroll row = new Payroll();
        row.setUserId(5L);
        when(payrollRepository.findByUserId(5L)).thenReturn(List.of(row));
        when(userSnapshotService.findById(5L)).thenReturn(Optional.empty());
        assertEquals(1, payrollService.findVisible().size());
    }

    @Test
    void findForUser_deniedForOtherEmployee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "a", "EMPLOYEE", "A"));
        assertThrows(BusinessException.class, () -> payrollService.findForUser(99L));
    }

    @Test
    void save_hrOnly() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        UserSnapshot snap = new UserSnapshot();
        snap.setId(3L);
        when(userSnapshotService.requireById(3L)).thenReturn(snap);

        Payroll input = new Payroll();
        input.setUserId(3L);
        input.setBaseSalary(BigDecimal.valueOf(1000));
        input.setBonus(BigDecimal.ZERO);
        input.setDeductions(BigDecimal.ZERO);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userSnapshotService.findById(3L)).thenReturn(Optional.of(snap));

        Payroll saved = payrollService.save(input);
        assertEquals(BigDecimal.valueOf(1000), saved.getTotalSalary());
    }

    @Test
    void save_rejectsNonHr() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        assertThrows(BusinessException.class, () -> payrollService.save(new Payroll()));
    }

    @Test
    void update_hrRecalculatesTotal() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Payroll existing = new Payroll();
        existing.setId(4L);
        existing.setUserId(3L);
        when(payrollRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(payrollRepository.save(existing)).thenReturn(existing);
        when(userSnapshotService.findById(3L)).thenReturn(Optional.empty());

        Payroll patch = new Payroll();
        patch.setBaseSalary(BigDecimal.valueOf(2000));
        patch.setBonus(BigDecimal.valueOf(100));
        patch.setDeductions(BigDecimal.valueOf(50));

        Payroll updated = payrollService.update(4L, patch);
        assertEquals(BigDecimal.valueOf(2050), updated.getTotalSalary());
    }

    @Test
    void delete_hrOnly() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        payrollService.delete(7L);
        verify(payrollRepository).deleteById(7L);
    }

    @Test
    void delete_rejectsEmployee() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        assertThrows(BusinessException.class, () -> payrollService.delete(7L));
    }

    @Test
    void findVisible_hrSeesAll() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        Payroll row = new Payroll();
        row.setUserId(1L);
        when(payrollRepository.findAll()).thenReturn(List.of(row));
        when(userSnapshotService.findById(1L)).thenReturn(Optional.empty());
        assertEquals(1, payrollService.findVisible().size());
    }

    @Test
    void findForUser_hrCanViewOtherUser() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        when(payrollRepository.findByUserId(9L)).thenReturn(List.of());
        assertEquals(0, payrollService.findForUser(9L).size());
    }

    @Test
    void save_extractsUserIdFromUserMap() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        UserSnapshot snap = new UserSnapshot();
        snap.setId(8L);
        when(userSnapshotService.requireById(8L)).thenReturn(snap);
        when(userSnapshotService.findById(8L)).thenReturn(Optional.of(snap));

        Payroll input = new Payroll();
        input.setUser(java.util.Map.of("id", 8));
        input.setBaseSalary(BigDecimal.valueOf(500));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(inv -> inv.getArgument(0));

        Payroll saved = payrollService.save(input);
        assertEquals(8L, saved.getUserId());
    }

    @Test
    void save_rejectsMissingUserId() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "hr", "HR", "HR"));
        assertThrows(BusinessException.class, () -> payrollService.save(new Payroll()));
    }

    @Test
    void findVisible_adminSeesAll() {
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "a", "ADMIN", "A"));
        when(payrollRepository.findAll()).thenReturn(List.of());
        assertEquals(0, payrollService.findVisible().size());
    }

    @Test
    void findById_deniedForOtherEmployee() {
        Payroll row = new Payroll();
        row.setId(1L);
        row.setUserId(99L);
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(row));
        when(currentUserProvider.requireCurrentUser()).thenReturn(new JwtUserPrincipal(1L, "e", "EMPLOYEE", "E"));
        assertThrows(BusinessException.class, () -> payrollService.findById(1L));
    }
}
