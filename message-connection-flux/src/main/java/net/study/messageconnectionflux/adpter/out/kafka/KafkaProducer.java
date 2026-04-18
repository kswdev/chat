package net.study.messageconnectionflux.adpter.out.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.application.port.out.EventProducer;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer implements EventProducer {

    private final KafkaSender<String, String> kafkaSender;
    private final JsonUtil jsonUtil;

    @Value("${message-system.kafka.topics.message}")
    private String messageTopic;

    @Value("${message-system.kafka.topics.request}")
    private String requestTopic;

    @Value("${message-system.kafka.topics.push}")
    private String pushTopic;

    public Mono<Void> sendMessageUsingPartitionKey(
            ChannelId channelId,
            UserId userId,
            RecordInterface recordInterface,
            Runnable errorCallback
    ) {
        String partitionKey = "%d-%d".formatted(channelId.id(), userId.id());

        return jsonUtil.toJson(recordInterface)
                .map(record -> send(messageTopic, partitionKey, record, errorCallback))
                .orElse(Mono.empty());
    }

    public Mono<Void> sendRequest(RecordInterface recordInterface, Runnable errorCallback) {
        return jsonUtil.toJson(recordInterface)
                .map(record -> send(requestTopic, null, record, errorCallback))
                .orElse(Mono.empty());
    }

    public Mono<Void> sendPushNotification(RecordInterface recordInterface) {
        return jsonUtil.toJson(recordInterface)
                .map(record -> send(pushTopic, null, record, null))
                .orElse(Mono.empty());
    }

    // =========================
    // 공통 send 로직
    // =========================
    private Mono<Void> send(String topic, String key, String value, Runnable errorCallback) {
        SenderRecord<String, String, Void> record =
                SenderRecord.create(topic, null, null, key, value, null);

        return kafkaSender.send(Mono.just(record))
                .doOnNext(result -> {
                    if (result.exception() != null) {
                        log.error("Kafka send failed. record: {} with key: {} to topic: {}, cause: {}",
                                value, key, topic, result.exception().getMessage());

                        if (errorCallback != null)
                            errorCallback.run();
                    } else {
                        log.info("Kafka send success. {}, topic: {} with key: {}",
                                value, topic, key);
                    }
                })
                .then();
    }
}
