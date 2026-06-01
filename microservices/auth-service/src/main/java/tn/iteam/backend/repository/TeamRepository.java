package tn.iteam.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.iteam.backend.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);

    Optional<Team> findByTeamLeader_Id(Long teamLeaderId);

    List<Team> findAllByTeamLeader_Id(Long teamLeaderId);
}

