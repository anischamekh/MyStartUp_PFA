package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.service.TeamService;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    @Override
    public Team findById(Long id) {
        return teamRepository.findById(id).orElseThrow(() -> new BusinessException("Team not found"));
    }

    @Override
    public Team create(Team team) {
        if (team.getId() != null) team.setId(null);
        return teamRepository.save(team);
    }

    @Override
    public Team update(Long id, Team team) {
        Team existing = findById(id);
        existing.setName(team.getName());
        existing.setTeamLeader(team.getTeamLeader());
        if (team.getSpeciality() != null) {
            existing.setSpeciality(team.getSpeciality());
        }
        return teamRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        teamRepository.deleteById(id);
    }
}

