package net.study.messagepush.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushMessageConsumer {

    @KafkaListener(
            topics = "${message-system.kafka.listeners.push.topic}",
            groupId = "${message-system.kafka.listeners.push.group-id}",
            concurrency = "${message-system.kafka.listeners.push.concurrency}")
    public void consumeMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("received record from: {} with key: {} and value: {} partition: {}, offset: {}",
                consumerRecord.topic(), consumerRecord.key(), consumerRecord.value(), consumerRecord.partition(), consumerRecord.offset());

        acknowledgment.acknowledge();
    }
}
