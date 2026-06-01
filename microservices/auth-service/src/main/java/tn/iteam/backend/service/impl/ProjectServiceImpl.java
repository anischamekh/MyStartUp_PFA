package tn.iteam.backend.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.entity.RoleName;
import tn.iteam.backend.entity.Team;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.repository.TeamRepository;
import tn.iteam.backend.service.ProjectService;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;

    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper
    ) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public Project findById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new BusinessException("Project not found"));
    }

    @Override
    public Project create(Project project) {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.MANAGER) {
            throw new BusinessException("Only MANAGER can create projects");
        }
        if (project.getId() != null) {
            project.setId(null);
        }
        project.setManager(me);
        Set<Team> resolved = resolveTeams(project.getTeams());
        project.setTeams(resolved);
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        Project saved = projectRepository.save(project);
        notifyLeadersForTeamIds(
                saved,
                saved.getTeams().stream().map(Team::getId).collect(Collectors.toSet())
        );
        return saved;
    }

    @Override
    public Project update(Long id, Project project) {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.MANAGER) {
            throw new BusinessException("Only MANAGER can update projects");
        }
        Project existing = findById(id);
        if (existing.getManager() == null
                || existing.getManager().getId() == null
                || !existing.getManager().getId().equals(me.getId())) {
            throw new BusinessException("You can only update your own projects");
        }

        Set<Long> previousTeamIds = existing.getTeams().stream().map(Team::getId).collect(Collectors.toSet());

        existing.setName(project.getName());
        existing.setDescription(project.getDescription());
        existing.setStartDate(project.getStartDate());
        existing.setEndDate(project.getEndDate());
        if (project.getStatus() != null) {
            existing.setStatus(project.getStatus());
        }
        if (project.getProgress() != null) {
            existing.setProgress(project.getProgress());
        }
        existing.setTeams(resolveTeams(project.getTeams()));

        Project saved = projectRepository.save(existing);

        Set<Long> newTeamIds = saved.getTeams().stream().map(Team::getId).collect(Collectors.toSet());
        Set<Long> added = new HashSet<>(newTeamIds);
        added.removeAll(previousTeamIds);
        notifyLeadersForTeamIds(saved, added);

        return saved;
    }

    @Override
    public void delete(Long id) {
        User me = currentUserProvider.requireCurrentUser();
        if (me.getRole() == null || me.getRole().getName() != RoleName.MANAGER) {
            throw new BusinessException("Only MANAGER can delete projects");
        }
        Project existing = findById(id);
        if (existing.getManager() == null
                || existing.getManager().getId() == null
                || !existing.getManager().getId().equals(me.getId())) {
            throw new BusinessException("You can only delete your own projects");
        }
        projectRepository.deleteById(id);
    }

    private Set<Team> resolveTeams(Set<Team> teams) {
        if (teams == null || teams.isEmpty()) {
            return new HashSet<>();
        }
        Set<Team> resolved = new HashSet<>();
        for (Team t : teams) {
            if (t == null || t.getId() == null) {
                continue;
            }
            Team full = teamRepository.findById(t.getId()).orElseThrow(() -> new BusinessException("Team not found"));
            resolved.add(full);
        }
        return resolved;
    }

    private void notifyLeadersForTeamIds(Project project, Set<Long> teamIds) {
        for (Long tid : teamIds) {
            Team t = teamRepository.findById(tid).orElse(null);
            if (t != null && t.getTeamLeader() != null) {
                notificationHelper.notify(
                        t.getTeamLeader(),
                        NotificationType.PROJECT_ASSIGNED,
                        "Project \"" + project.getName() + "\" was assigned to your team"
                );
            }
        }
    }
}
