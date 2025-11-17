package net.study.messageconnection.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.RecordInterface;
import net.study.messageconnection.handler.kafka.RecordDispatcher;
import net.study.messageconnection.util.JsonUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListenTopicConsumer {

    private final ListenTopicCreator listenTopicCreator;
    private final RecordDispatcher recordDispatcher;
    private final JsonUtil jsonUtil;

    @KafkaListener(
            topics = "{__Listener.getListenTopic()}",
            groupId = "{__Listener.getConsumerGroupId()}",
            concurrency = "${message-system.kafka.listeners.push.concurrency}")
    public void listenTopicConsumerGroup(
            ConsumerRecord<String, String> consumerRecord,
            Acknowledgment acknowledgment
    ) {
        log.info("received record from: {} with key: {} and value: {} partition: {}, offset: {}",
                consumerRecord.topic(), consumerRecord.key(), consumerRecord.value(), consumerRecord.partition(), consumerRecord.offset());

        jsonUtil.fromJson(consumerRecord.value(), RecordInterface.class)
                .ifPresentOrElse(recordDispatcher::dispatch, () -> log.error("Record handling failed. cause: Invalid record: {}", consumerRecord.value()));
        acknowledgment.acknowledge();
    }

    public String getListenTopic() {
        return listenTopicCreator.getListenTopic();
    }

    public String getConsumerGroupId() {
        return listenTopicCreator.getConsumerGroupId();
    }
}
