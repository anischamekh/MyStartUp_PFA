package tn.iteam.backend.messaging;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tn.iteam.common.events.KafkaTopics;
import tn.iteam.common.events.UserDeletedEvent;

@ExtendWith(MockitoExtension.class)
class UserEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserEventPublisher publisher;

    @Test
    void publishUserDeleted_sendsToUserEventsTopic() {
        publisher.publishUserDeleted(5L);
        verify(kafkaTemplate).send(eq(KafkaTopics.USER_EVENTS), org.mockito.ArgumentMatchers.any(UserDeletedEvent.class));
    }
}
