package tn.iteam.backend.service;

import java.util.List;
import tn.iteam.backend.entity.LeaveRequest;

public interface LeaveRequestService {
    List<LeaveRequest> findAll();
    LeaveRequest findById(Long id);
    List<LeaveRequest> findMine();
    /** HR / MANAGER / ADMIN: history for a specific user. */
    List<LeaveRequest> findForUser(Long userId);
    LeaveRequest request(LeaveRequest leaveRequest); // employee requests
    LeaveRequest approve(Long id); // manager approves
    LeaveRequest reject(Long id);  // manager rejects
    void delete(Long id);
}

