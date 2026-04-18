package net.study.messageconnectionflux.adpater.out.kafka

import lombok.extern.slf4j.Slf4j
import net.study.messageconnectionflux.application.port.out.EventProducer
import net.study.messageconnectionflux.domain.user.UserId
import net.study.messageconnectionflux.application.dto.kafka.AcceptRequestRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.test.StepVerifier
import spock.lang.Specification

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class KafkaProducerIntegrationSpec extends Specification {

    @Autowired
    private EventProducer kafkaProducer

    KafkaReceiver<String, String> receiver

    def setup() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19094, localhost:19095, localhost:19096",
                ConsumerConfig.GROUP_ID_CONFIG, "listen-group-1",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        );

        ReceiverOptions<String, String> options =
                ReceiverOptions.<String, String>create(props)
                        .subscription(List.of("message-request-1"))
                        .addAssignListener(partitions -> log.info("assigned: {}", partitions))
                        .addRevokeListener(partitions -> log.info("revoked: {}", partitions))

        receiver = KafkaReceiver.create(options)
    }

    def "AcceptRequest 요청 카프카 이벤트 처리."() {
        given:
        UserId userId = new UserId(1L)
        String username = "test"
        AcceptRequestRecord record = new AcceptRequestRecord(userId, username)

        when:
        kafkaProducer.sendRequest(record, _ as Runnable)

        then:
        StepVerifier.create(
                receiver.receive()
                        .map(r -> r.value())
                        .take(1)
        )
                .expectNextMatches(value -> value.contains("test"))
                .verifyComplete()
    }
}
