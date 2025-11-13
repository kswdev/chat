package net.study.messageconnection.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(DefaultKafkaProducerFactory<String, String> producerFactory) {
        Producer<String, String> producer = producerFactory.createProducer();
        producer.close();
        log.info("KafkaProducer initialized.");
        return new KafkaTemplate<>(producerFactory);
    }
}
