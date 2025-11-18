package net.study.messagesystem.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.kafka.RecordInterface;
import net.study.messagesystem.handler.kafka.RecordDispatcher;
import net.study.messagesystem.util.JsonUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final RecordDispatcher recordDispatcher;
    private final JsonUtil jsonUtil;

    @KafkaListener(
            topics = "${message-system.kafka.listeners.message.topic}",
            groupId = "${message-system.kafka.listeners.message.group-id}",
            concurrency = "${message-system.kafka.listeners.message.concurrency}")
    public void messageTopicConsumerGroup(
            ConsumerRecord<String, String> consumerRecord,
            Acknowledgment acknowledgment
    ) {
        logInfo("messageTopicConsumerGroup", consumerRecord);

        jsonUtil.fromJson(consumerRecord.value(), RecordInterface.class)
                .ifPresentOrElse(recordDispatcher::dispatch, () -> logError("messageTopicConsumerGroup", consumerRecord));

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${message-system.kafka.listeners.request.topic}",
            groupId = "${message-system.kafka.listeners.request.group-id}",
            concurrency = "${message-system.kafka.listeners.request.concurrency}")
    public void requestTopicConsumerGroup(
            ConsumerRecord<String, String> consumerRecord,
            Acknowledgment acknowledgment
    ) {
        logInfo("requestTopicConsumerGroup", consumerRecord);

        jsonUtil.fromJson(consumerRecord.value(), RecordInterface.class)
                .ifPresentOrElse(recordDispatcher::dispatch, () -> logError("requestTopicConsumerGroup", consumerRecord));

        acknowledgment.acknowledge();
    }

    private static void logInfo(String listener, ConsumerRecord<String, String> consumerRecord) {
        log.info("Received listener: {}, record from: {} with key: {} and value: {} partition: {}, offset: {}",
                listener,
                consumerRecord.topic(),
                consumerRecord.key(),
                consumerRecord.value(),
                consumerRecord.partition(),
                consumerRecord.offset());
    }

    private static void logError(String listener, ConsumerRecord<String, String> consumerRecord) {
        log.error("Received listener: {}, record from: {} with key: {} and value: {} partition: {}, offset: {}",
                listener,
                consumerRecord.topic(),
                consumerRecord.key(),
                consumerRecord.value(),
                consumerRecord.partition(),
                consumerRecord.offset());
    }
}
