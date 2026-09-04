package net.study.messageconnectionflux.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * server.id 기반으로 Kafka topic/consumer group 이름을 만들던 ListenTopicCreator를
 * 대체합니다. 이제 이 인스턴스를 식별하는 값은 k8s Downward API로 주입되는 POD_NAME이고,
 * Kafka topic이 아니라 Redis Pub/Sub 채널 이름을 만드는 데만 씁니다.
 */
@Component
public class PodIdentity {

    @Getter
    private final String podName;
    private final String channelPrefix;

    public PodIdentity(
            @Value("${app.pod-name}") String podName,
            @Value("${message-system.redis.channel-prefix}") String channelPrefix
    ) {
        this.podName = podName;
        this.channelPrefix = channelPrefix;
    }

    public String getDeliveryChannel() {
        return "%s:%s".formatted(channelPrefix, podName);
    }
}
