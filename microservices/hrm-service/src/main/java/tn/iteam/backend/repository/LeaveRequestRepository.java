package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iteam.backend.entity.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByManagerId(Long managerId);

    void deleteByEmployeeId(Long employeeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE LeaveRequest l SET l.managerId = null WHERE l.managerId = :userId")
    int clearManagerForUser(@Param("userId") Long userId);
}
