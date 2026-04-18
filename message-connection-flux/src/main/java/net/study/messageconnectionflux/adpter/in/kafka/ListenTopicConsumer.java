package net.study.messageconnectionflux.adpter.in.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListenTopicConsumer {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final RecordDispatcher recordDispatcher;
    private final JsonUtil jsonUtil;

    @PostConstruct
    public void consume() {
        kafkaReceiver.receive()
                .flatMap(record -> {
                    log.info("received record from: {} with key: {} and value: {} partition: {}, offset: {}",
                            record.topic(), record.key(), record.value(), record.partition(), record.offset());

                    return jsonUtil.fromJson(record.value(), RecordInterface.class)
                            .flatMap(recordInterface -> Mono.fromRunnable(() -> recordDispatcher.dispatch(recordInterface)))
                            .doOnError(e -> log.error("Error processing record: {}", record.value(), e))
                            .doOnSuccess(v -> record.receiverOffset().acknowledge());
                }, 3)
                .subscribe();
    }
}
