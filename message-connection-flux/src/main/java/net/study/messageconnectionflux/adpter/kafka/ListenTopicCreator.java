package net.study.messageconnectionflux.adpter.kafka;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ListenTopicCreator {

    private final String prefixListenTopic;
    private final String prefixGroupId;
    private final int partitions;
    private final short replicationFactor;
    private final String prefixServerId;
    private final String bootstrapServers;

    public ListenTopicCreator(
            KafkaAdmin admin,
            @Value("${message-system.kafka.listeners.push.prefix-group-id}") String prefixGroupId,
            @Value("${server.id}") String prefixServerId,
            @Value("${message-system.kafka.listeners.push.prefix-topic}") String prefixListenTopic,
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${message-system.kafka.listeners.push.partitions}") int partitions,
            @Value("${message-system.kafka.listeners.push.replications-factor}") short replicationFactor
    ) {
        this.prefixGroupId = prefixGroupId;
        this.prefixServerId = prefixServerId;
        this.prefixListenTopic = prefixListenTopic;
        this.bootstrapServers = bootstrapServers;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
    }

    @PostConstruct
    public void init() {
        createTopic(getListenTopic(), partitions, replicationFactor);
    }

    public void createTopic(String topicName, int partitions, short replicationFactor) {
        Map<String, Object> configs = Map.of(
                "bootstrap.servers", bootstrapServers,
                AdminClientConfig.RETRIES_CONFIG, 5,
                AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, 1000
        );

        try(AdminClient adminClient = AdminClient.create(configs)) {
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            CreateTopicsResult topicsResult = adminClient.createTopics(List.of(newTopic));
            topicsResult.values().forEach((topic, future) -> {
                try {
                    future.get();
                    log.info("Created topic. topic: {}", topicName);
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof TopicExistsException) {
                         log.info("Topic already exists. topic: {}", topicName);
                    } else {
                        log.error("Failed to create topic. topic: {}", topicName, ex);
                        throw new RuntimeException(ex);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    log.error("Failed to create topic. topic: {}", topicName, ex);
                }
            });
        }
    }

    public String getListenTopic() {
        return "%s-%s".formatted(prefixListenTopic, prefixServerId);
    }

    public String getConsumerGroupId() {
        return "%s-%s".formatted(prefixGroupId, prefixServerId);
    }
}
