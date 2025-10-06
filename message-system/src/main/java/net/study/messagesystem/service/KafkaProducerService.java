package net.study.messagesystem.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.kafka.outbound.RecordInterface;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtil jsonUtil;

    @Value("${message-system.kafka.listeners.push.topic}")
    private String pushTopic;

    public void sendMessage(String topic, String key, String message) {
        SendResult<String, String> sendResult;
        try {
            if (key == null || key.isEmpty())
                sendResult = kafkaTemplate.send(topic, message).get();
            else
                sendResult = kafkaTemplate.send(topic, key, message).get();

            log.info("sendResult: {}", sendResult);
        } catch (Exception e) {
            log.error("Kafka send failed. topic: {}, key: {}, message: {}", topic, key, message);
        }
    }

    public void sendPushNotification(RecordInterface recordInterface) {
        jsonUtil.toJson(recordInterface)
                .ifPresent(record ->
                        kafkaTemplate.send(pushTopic, record)
                                     .whenComplete((sendResult, throwable) -> {
                                         if (throwable != null)
                                             log.error("Kafka send failed. record: {}, cause: {}", record, throwable.getMessage());
                                         else
                                             log.info("Kafka send success. topic: {}, record: {}", pushTopic, sendResult.getProducerRecord().value());
                                     })
        );
    }
}
