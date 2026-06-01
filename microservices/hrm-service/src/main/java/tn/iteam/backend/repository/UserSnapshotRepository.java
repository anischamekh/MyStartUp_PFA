package tn.iteam.backend.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.UserSnapshot;

public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, Long> {
    List<UserSnapshot> findByTeamId(Long teamId);

    List<UserSnapshot> findByIdIn(Collection<Long> ids);
}
