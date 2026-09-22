package net.study.messagesocial.adapter.out.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesocial.adapter.out.notification.dto.RecordInterface;
import net.study.messagesocial.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtil jsonUtil;

    @Value("${message-social.kafka.topics.push}")
    private String pushTopic;

    public void sendPushNotification(RecordInterface recordInterface) {
        jsonUtil.toJson(recordInterface)
                .ifPresent(record -> kafkaTemplate.send(pushTopic, record)
                        .whenComplete((result, throwable) -> {
                            if (throwable != null)
                                log.error("Kafka send failed. record: {} to topic: {}, cause: {}", record, pushTopic, throwable.getMessage());
                            else
                                log.info("Kafka send success. {} to topic: {}", record, pushTopic);
                        }));
    }
}
