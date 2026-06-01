package tn.iteam.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import tn.iteam.common.events.KafkaTopics;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public NewTopic leaveEventsTopic() {
        return TopicBuilder.name(KafkaTopics.LEAVE_EVENTS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_EVENTS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic trainingRemindersTopic() {
        return TopicBuilder.name(KafkaTopics.TRAINING_REMINDERS).partitions(1).replicas(1).build();
    }
}
