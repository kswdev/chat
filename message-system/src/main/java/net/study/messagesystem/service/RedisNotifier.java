package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.kafka.RecordInterface;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 예전엔 KafkaProducer.sendResponse(topic, ...) / sendMessageUsingPartitionKey(topic, ...)가
 * "이 유저가 붙어있는 인스턴스 전용 Kafka topic"으로 응답을 발행했습니다.
 * 이제 그 topic 이름 자리에는 Kafka topic이 아니라 pod 이름(POD_NAME)이 들어오기 때문에,
 * 발행 수단도 Kafka가 아니라 그 pod가 구독 중인 Redis Pub/Sub 채널로 바뀌어야 합니다.
 *
 * 채널 이름 규칙(ws:deliver:{podName})은 message-connection-flux의 PodIdentity가
 * 구독하는 채널 이름과 정확히 일치해야 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisNotifier {

    private final StringRedisTemplate stringRedisTemplate;
    private final JsonUtil jsonUtil;

    @Value("${message-system.redis.channel-prefix}")
    private String channelPrefix;

    public void publish(String podName, RecordInterface recordInterface) {
        String channel = "%s:%s".formatted(channelPrefix, podName);

        jsonUtil.toJson(recordInterface).ifPresentOrElse(
                payload -> {
                    // Pub/Sub은 발행 시점에 구독자가 없으면 그냥 유실됩니다(디스크에 안 남음).
                    // Kafka topic과 달리 재시도/재처리 개념이 없다는 걸 감안하고 쓰는 자리입니다.
                    Long received = stringRedisTemplate.convertAndSend(channel, payload);
                    log.info("Redis publish. channel: {}, subscribers: {}", channel, received);
                },
                () -> log.error("Failed to serialize record for channel: {}", channel)
        );
    }
}
