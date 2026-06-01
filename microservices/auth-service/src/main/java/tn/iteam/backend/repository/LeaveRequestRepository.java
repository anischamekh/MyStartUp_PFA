package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iteam.backend.entity.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long userId);
    List<LeaveRequest> findByManagerId(Long managerId);

    void deleteByEmployee_Id(Long employeeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE LeaveRequest l SET l.manager = null WHERE l.manager.id = :userId")
    int clearManagerForUser(@Param("userId") Long userId);
}

