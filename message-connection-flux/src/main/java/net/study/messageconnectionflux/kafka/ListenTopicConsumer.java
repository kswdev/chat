package net.study.messageconnectionflux.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.handler.kafka.RecordDispatcher;
import net.study.messageconnectionflux.util.JsonUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListenTopicConsumer {

    private final ListenTopicCreator listenTopicCreator;
    private final RecordDispatcher recordDispatcher;
    private final JsonUtil jsonUtil;

    @KafkaListener(
            topics = "#{__listener.getListenTopic()}",
            groupId = "#{__listener.getConsumerGroupId()}",
            concurrency = "${message-system.kafka.listeners.push.concurrency}")
    public void listenTopicConsumerGroup(
            ConsumerRecord<String, String> consumerRecord,
            Acknowledgment acknowledgment
    ) {
        log.info("received record from: {} with key: {} and value: {} partition: {}, offset: {}",
                consumerRecord.topic(), consumerRecord.key(), consumerRecord.value(), consumerRecord.partition(), consumerRecord.offset());

        jsonUtil.fromJson(consumerRecord.value(), RecordInterface.class)
                .doOnNext(recordDispatcher::dispatch)
                .switchIfEmpty(Mono.fromRunnable(() ->
                        log.error("Record handling failed. cause: Invalid record: {}", consumerRecord.value())
                ))
                .subscribe();

        acknowledgment.acknowledge();
    }

    public String getListenTopic() {
        return listenTopicCreator.getListenTopic();
    }

    public String getConsumerGroupId() {
        return listenTopicCreator.getConsumerGroupId();
    }
}
