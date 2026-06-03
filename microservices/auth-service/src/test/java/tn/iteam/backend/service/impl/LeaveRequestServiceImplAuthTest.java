package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.iteam.backend.entity.Role;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.LeaveRequestRepository;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplAuthTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private EmployeeProfileRepository employeeProfileRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    @Test
    void findById_missing() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> leaveRequestService.findById(1L));
    }

    @Test
    void findForUser_deniedForEmployee() {
        User emp = new User();
        emp.setId(1L);
        Role role = new Role();
        role.setName(RoleName.EMPLOYEE);
        emp.setRole(role);
        when(currentUserProvider.requireCurrentUser()).thenReturn(emp);
        assertThrows(BusinessException.class, () -> leaveRequestService.findForUser(99L));
    }
}
