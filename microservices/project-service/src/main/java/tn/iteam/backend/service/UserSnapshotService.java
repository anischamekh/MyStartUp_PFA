package tn.iteam.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.client.AuthServiceClient;
import tn.iteam.backend.entity.UserSnapshot;
import tn.iteam.backend.repository.UserSnapshotRepository;
import tn.iteam.common.dto.UserSummaryDto;
import tn.iteam.common.events.EmployeeUpdatedEvent;

@Service
@Transactional
public class UserSnapshotService {

    private final UserSnapshotRepository userSnapshotRepository;
    private final AuthServiceClient authServiceClient;

    public UserSnapshotService(UserSnapshotRepository userSnapshotRepository, AuthServiceClient authServiceClient) {
        this.userSnapshotRepository = userSnapshotRepository;
        this.authServiceClient = authServiceClient;
    }

    public Map<Long, UserSnapshot> mapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return userSnapshotRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(UserSnapshot::getId, Function.identity()));
    }

    public void applyEmployeeUpdated(EmployeeUpdatedEvent event) {
        UserSnapshot snapshot = userSnapshotRepository.findById(event.userId()).orElse(new UserSnapshot());
        snapshot.setId(event.userId());
        snapshot.setUsername(event.username());
        snapshot.setFullName(event.fullName());
        snapshot.setEmail(event.email());
        snapshot.setRoleName(event.role());
        snapshot.setTeamId(event.teamId());
        snapshot.setTeamName(event.teamName());
        userSnapshotRepository.save(snapshot);
    }

    public void deleteUser(Long userId) {
        userSnapshotRepository.deleteById(userId);
    }

    public Optional<UserSnapshot> findById(Long id) {
        return userSnapshotRepository.findById(id);
    }

    public UserSnapshot requireById(Long id) {
        return userSnapshotRepository.findById(id).orElseGet(() -> syncFromAuth(id));
    }

    private UserSnapshot syncFromAuth(Long userId) {
        UserSummaryDto dto = authServiceClient.getUser(userId);
        applyEmployeeUpdated(new EmployeeUpdatedEvent(
                dto.id(), dto.username(), dto.fullName(), dto.email(), dto.role(),
                dto.teamId(), dto.teamName(), null, "SYNC", java.time.Instant.now()));
        return userSnapshotRepository.findById(userId).orElseThrow();
    }
}
