package tn.iteam.backend.service.impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.NotificationType;
import tn.iteam.backend.entity.Project;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.messaging.EventPublisher;
import tn.iteam.backend.repository.ProjectRepository;
import tn.iteam.backend.service.ProjectService;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.dto.TeamSummaryDto;
import tn.iteam.common.events.ProjectCreatedEvent;
import tn.iteam.common.security.JwtUserPrincipal;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationHelper notificationHelper;
    private final EventPublisher eventPublisher;
    private final AuthServiceClient authServiceClient;
    private final UserSnapshotService userSnapshotService;

    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            CurrentUserProvider currentUserProvider,
            NotificationHelper notificationHelper,
            EventPublisher eventPublisher,
            AuthServiceClient authServiceClient,
            UserSnapshotService userSnapshotService
    ) {
        this.projectRepository = projectRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationHelper = notificationHelper;
        this.eventPublisher = eventPublisher;
        this.authServiceClient = authServiceClient;
        this.userSnapshotService = userSnapshotService;
    }

    @Override
    public List<Project> findAll() {
        return enrich(projectRepository.findAll());
    }

    @Override
    public Project findById(Long id) {
        return enrich(projectRepository.findById(id).orElseThrow(() -> new BusinessException("Project not found")));
    }

    @Override
    public Project create(Project project) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"MANAGER".equals(me.role())) {
            throw new BusinessException("Only MANAGER can create projects");
        }
        if (project.getId() != null) {
            project.setId(null);
        }
        project.setManagerUserId(me.userId());
        project.setTeamIds(resolveTeamIds(project));
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        Project saved = enrich(projectRepository.save(project));
        notifyLeadersForTeamIds(saved, saved.getTeamIds());
        eventPublisher.publishProjectCreated(new ProjectCreatedEvent(
                saved.getId(),
                saved.getName(),
                me.userId(),
                Instant.now()
        ));
        return saved;
    }

    @Override
    public Project update(Long id, Project project) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"MANAGER".equals(me.role())) {
            throw new BusinessException("Only MANAGER can update projects");
        }
        Project existing = findById(id);
        if (!me.userId().equals(existing.getManagerUserId())) {
            throw new BusinessException("You can only update your own projects");
        }

        Set<Long> previousTeamIds = new HashSet<>(existing.getTeamIds());
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
        existing.setTeamIds(resolveTeamIds(project));

        Project saved = enrich(projectRepository.save(existing));
        Set<Long> added = new HashSet<>(saved.getTeamIds());
        added.removeAll(previousTeamIds);
        notifyLeadersForTeamIds(saved, added);
        return saved;
    }

    @Override
    public void delete(Long id) {
        JwtUserPrincipal me = currentUserProvider.requireCurrentUser();
        if (!"MANAGER".equals(me.role())) {
            throw new BusinessException("Only MANAGER can delete projects");
        }
        Project existing = findById(id);
        if (!me.userId().equals(existing.getManagerUserId())) {
            throw new BusinessException("You can only delete your own projects");
        }
        projectRepository.deleteById(id);
    }

    private Set<Long> resolveTeamIds(Project project) {
        Set<Long> ids = new HashSet<>();
        if (project.getTeamIds() != null) {
            ids.addAll(project.getTeamIds());
        }
        if (project.getTeams() != null) {
            for (Map<String, Object> team : project.getTeams()) {
                if (team.get("id") != null) {
                    ids.add(Long.valueOf(team.get("id").toString()));
                }
            }
        }
        for (Long teamId : ids) {
            authServiceClient.getTeam(teamId);
        }
        return ids;
    }

    private void notifyLeadersForTeamIds(Project project, Set<Long> teamIds) {
        for (Long teamId : teamIds) {
            TeamSummaryDto team = authServiceClient.getTeam(teamId);
            if (team.teamLeaderUserId() != null) {
                notificationHelper.notify(
                        team.teamLeaderUserId(),
                        NotificationType.PROJECT_ASSIGNED,
                        "Project \"" + project.getName() + "\" was assigned to your team"
                );
            }
        }
    }

    private List<Project> enrich(List<Project> projects) {
        return projects.stream().map(this::enrich).toList();
    }

    private Project enrich(Project project) {
        if (project.getManagerUserId() != null) {
            project.enrichManager(userSnapshotService.findById(project.getManagerUserId()).orElse(null));
        }
        Set<Map<String, Object>> teamViews = project.getTeamIds().stream()
                .map(id -> {
                    try {
                        TeamSummaryDto t = authServiceClient.getTeam(id);
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("id", t.id());
                        map.put("name", t.name());
                        return map;
                    } catch (Exception ex) {
                        return Map.<String, Object>of("id", id);
                    }
                })
                .collect(Collectors.toSet());
        project.setTeams(teamViews);
        return project;
    }
}
