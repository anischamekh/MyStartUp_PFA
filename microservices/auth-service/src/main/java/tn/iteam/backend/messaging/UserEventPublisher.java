package tn.iteam.backend.messaging;

import java.time.Instant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tn.iteam.backend.entity.EmployeeProfile;
import tn.iteam.backend.entity.User;
import tn.iteam.common.events.EmployeeUpdatedEvent;
import tn.iteam.common.events.KafkaTopics;
import tn.iteam.common.events.UserDeletedEvent;

@Component
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserUpdated(User user, EmployeeProfile profile, String eventType) {
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, new EmployeeUpdatedEvent(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() == null ? null : user.getRole().getName().name(),
                profile == null || profile.getTeam() == null ? null : profile.getTeam().getId(),
                profile == null || profile.getTeam() == null ? null : profile.getTeam().getName(),
                profile == null ? null : profile.getRemainingLeaveDays(),
                eventType,
                Instant.now()
        ));
    }

    public void publishUserDeleted(Long userId) {
        kafkaTemplate.send(KafkaTopics.USER_EVENTS, new UserDeletedEvent(userId, Instant.now()));
    }
}
