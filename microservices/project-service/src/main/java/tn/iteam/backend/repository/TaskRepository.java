package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iteam.backend.entity.Task;
import tn.iteam.backend.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToUserId(Long userId);

    List<Task> findByCreatedByUserId(Long userId);

    List<Task> findByProjectId(Long projectId);

    boolean existsByAssignedToUserIdAndStatusIn(Long userId, List<TaskStatus> statuses);

    @Modifying
    @Query("UPDATE Task t SET t.assignedToUserId = null WHERE t.assignedToUserId = :userId")
    void clearAssignedToForUser(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Task t SET t.createdByUserId = null WHERE t.createdByUserId = :userId")
    void clearCreatedByForUser(@Param("userId") Long userId);
}
