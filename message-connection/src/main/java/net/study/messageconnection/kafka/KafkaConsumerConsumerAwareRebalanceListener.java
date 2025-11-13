package net.study.messageconnection.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class KafkaConsumerConsumerAwareRebalanceListener implements ConsumerAwareRebalanceListener {

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        log.info("kafka consumer: {}, assigned: {}", consumer.toString(), partitions.toString());
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        log.info("kafka assigned: {}",partitions.toString());
    }
}
