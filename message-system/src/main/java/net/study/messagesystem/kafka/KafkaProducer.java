package net.study.messagesystem.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.RecordInterface;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtil jsonUtil;

    @Value("${message-system.kafka.listeners.push.topic}")
    private String pushTopic;

    public void sendMessageUsingPartitionKey(String topic, ChannelId channelId, UserId userId, RecordInterface recordInterface, Runnable errorCallback) {
        String partitionKey = "%d-%d".formatted(channelId.id(), userId.id());
        jsonUtil.toJson(recordInterface)
                .ifPresent(record -> kafkaTemplate.send(topic, partitionKey, record)
                        .whenComplete(logResult(topic, record, partitionKey, errorCallback)));
    }

    public void sendResponse(String topic, RecordInterface recordInterface, Runnable errorCallback) {
        jsonUtil.toJson(recordInterface)
                .ifPresent(record -> kafkaTemplate.send(topic, record)
                        .whenComplete(logResult(topic, record, null, errorCallback)));
    }

    public void sendPushNotification(RecordInterface recordInterface) {
        jsonUtil.toJson(recordInterface)
                .ifPresent(record -> kafkaTemplate.send(pushTopic, record)
                        .whenComplete(logResult(pushTopic, record, null, null)));
    }

    private BiConsumer<SendResult<String, String>, Throwable> logResult(String topic, String record, String partitionKey, Runnable errorCallback) {
        return (sendResult, throwable) -> {
            if (throwable != null) {
                log.error("Kafka send failed. record: {} with key: {} to topic: {}, cause: {}",
                        record, topic, partitionKey, throwable.getMessage());
            } else {
                log.info("Kafka send success. {}, topic: {} with key: {}",
                        sendResult.getProducerRecord().value(), topic, partitionKey);

                if (errorCallback != null)
                    errorCallback.run();
            }
        };
    }
}
