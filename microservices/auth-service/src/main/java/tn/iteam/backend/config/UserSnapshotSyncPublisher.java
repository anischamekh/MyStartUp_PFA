package tn.iteam.backend.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.messaging.UserEventPublisher;
import tn.iteam.backend.repository.EmployeeProfileRepository;
import tn.iteam.backend.repository.UserRepository;

/**
 * Publishes Kafka user events for every auth user so HRM and Project services
 * populate {@code user_snapshots}. Required when users were inserted via SQL
 * (no API create) or after DB restore without re-running migration consumers.
 */
@Component
@Order(100)
public class UserSnapshotSyncPublisher implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSnapshotSyncPublisher.class);

    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final UserEventPublisher userEventPublisher;
    private final boolean enabled;

    public UserSnapshotSyncPublisher(
            UserRepository userRepository,
            EmployeeProfileRepository employeeProfileRepository,
            UserEventPublisher userEventPublisher,
            @Value("${app.sync.user-events-on-startup:true}") boolean enabled
    ) {
        this.userRepository = userRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.userEventPublisher = userEventPublisher;
        this.enabled = enabled;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return;
        }
        log.info("Publishing {} user snapshot event(s) to Kafka for HRM/Project sync", users.size());
        for (User user : users) {
            EmployeeProfile profile = employeeProfileRepository.findByUserId(user.getId()).orElse(null);
            userEventPublisher.publishUserUpdated(user, profile, "STARTUP_SYNC");
        }
    }
}
