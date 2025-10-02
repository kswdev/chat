package net.study.messagepush.config;

import lombok.extern.slf4j.Slf4j;
import net.study.messagepush.kafka.KafkaConsumerConsumerAwareRebalanceListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaConsumerConsumerAwareRebalanceListener awareRebalanceListener
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        containerFactory.getContainerProperties()
                .setConsumerRebalanceListener(awareRebalanceListener);

        log.info("set ackMode: {}", containerFactory.getContainerProperties().getAckMode());
        return containerFactory;
    }
}
