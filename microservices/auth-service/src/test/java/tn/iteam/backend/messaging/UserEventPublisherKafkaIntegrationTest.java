package tn.iteam.backend.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import tn.iteam.common.events.KafkaTopics;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = KafkaTopics.USER_EVENTS)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:kafka_auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=change-this-secret-in-real-projects-change-this-secret-key-32"
})
class UserEventPublisherKafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private UserEventPublisher publisher;

    @Test
    void publishUserDeleted_sendsMessageToTopic() {
        publisher.publishUserDeleted(99L);

        Consumer<String, String> consumer = createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, KafkaTopics.USER_EVENTS);
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

        assertThat(records.count()).isGreaterThan(0);
        ConsumerRecord<String, String> first = records.iterator().next();
        assertThat(first.value()).contains("99");
        consumer.close();
    }

    private Consumer<String, String> createConsumer() {
        var props = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<String, String>(props).createConsumer();
    }
}
