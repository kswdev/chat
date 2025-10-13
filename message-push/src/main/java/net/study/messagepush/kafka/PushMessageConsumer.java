package net.study.messagepush.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.dto.kafka.inbound.RecordInterface;
import net.study.messagepush.handler.RequestDispatcher;
import net.study.messagepush.util.JsonUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushMessageConsumer {

    private final JsonUtil jsonUtil;
    private final RequestDispatcher dispatcher;

    @KafkaListener(
            topics = "${message-system.kafka.listeners.push.topic}",
            groupId = "${message-system.kafka.listeners.push.group-id}",
            concurrency = "${message-system.kafka.listeners.push.concurrency}")
    public void consumeMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {

        try {
            log.info("received record from: {} with key: {} and value: {} partition: {}, offset: {}",
                    consumerRecord.topic(), consumerRecord.key(), consumerRecord.value(), consumerRecord.partition(), consumerRecord.offset());

            jsonUtil.fromJson(consumerRecord.value(), RecordInterface.class)
                    .ifPresent(dispatcher::dispatch);
        } catch (Exception e) {
            log.error("Record handling failed. cause: {}", e.getMessage());
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
