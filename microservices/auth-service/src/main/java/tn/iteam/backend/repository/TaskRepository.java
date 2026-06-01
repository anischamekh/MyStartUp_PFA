package tn.iteam.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.iteam.backend.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToId(Long userId);

    boolean existsByAssignedTo_IdAndStatusIn(Long userId, List<tn.iteam.backend.entity.TaskStatus> statuses);
    List<Task> findByCreatedById(Long userId);
    List<Task> findByProjectId(Long projectId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Task t SET t.assignedTo = null WHERE t.assignedTo.id = :userId")
    int clearAssignedToForUser(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Task t SET t.createdBy = null WHERE t.createdBy.id = :userId")
    int clearCreatedByForUser(@Param("userId") Long userId);
}

