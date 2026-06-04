package tn.iteam.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.service.TeamService;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final CurrentUserProvider currentUserProvider;

    public TeamServiceImpl(TeamRepository teamRepository, CurrentUserProvider currentUserProvider) {
        this.teamRepository = teamRepository;
        this.currentUserProvider = currentUserProvider;
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
        requireHrOrAdmin();
        if (team.getId() != null) team.setId(null);
        return teamRepository.save(team);
    }

    @Override
    public Team update(Long id, Team team) {
        requireHrOrAdmin();
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
        requireHrOrAdmin();
        teamRepository.deleteById(id);
    }

    private void requireHrOrAdmin() {
        User user = currentUserProvider.requireCurrentUser();
        if (user.getRole() == null) {
            throw new BusinessException("Not allowed");
        }
        RoleName role = user.getRole().getName();
        if (role != RoleName.HR && role != RoleName.ADMIN) {
            throw new BusinessException("Only HR or ADMIN can manage teams");
        }
    }
}

