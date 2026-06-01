package tn.iteam.backend.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tn.iteam.common.events.KafkaTopics;

@Component
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, Object event) {
        kafkaTemplate.send(topic, event);
    }

    public void publishTaskValidated(Object event) {
        publish(KafkaTopics.TASK_EVENTS, event);
    }

    public void publishProjectCreated(Object event) {
        publish(KafkaTopics.PROJECT_EVENTS, event);
    }

    public void publishNotification(Object event) {
        publish(KafkaTopics.NOTIFICATION_EVENTS, event);
    }
}
