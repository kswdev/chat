package net.study.messageconnectionflux.adpter.in.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.adpter.in.kafka.RecordDispatcher;
import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.config.PodIdentity;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * ListenTopicConsumer(Kafka)를 대체합니다. 이 pod 이름으로 만들어진 Redis 채널을
 * 구독하다가, message-system이 PUBLISH한 메시지를 받으면 기존과 동일하게
 * RecordDispatcher로 넘겨서 처리합니다. dispatch 이후 로직(핸들러, WebSocket 전달)은
 * 전송 수단이 Kafka든 Redis든 상관없이 그대로 재사용됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisChannelSubscriber {

    private final ReactiveRedisOperations<String, String> redisOperations;
    private final PodIdentity podIdentity;
    private final RecordDispatcher recordDispatcher;
    private final JsonUtil jsonUtil;

    @PostConstruct
    public void subscribe() {
        String channel = podIdentity.getDeliveryChannel();

        redisOperations.listenToChannel(channel)
                .doOnSubscribe(s -> log.info("Subscribed to Redis channel: {}", channel))
                .flatMap(message -> {
                    String payload = message.getMessage();
                    log.info("received message from channel: {} payload: {}", channel, payload);

                    return jsonUtil.fromJson(payload, RecordInterface.class)
                            .flatMap(recordInterface -> Mono.fromRunnable(() -> recordDispatcher.dispatch(recordInterface)))
                            .doOnError(e -> log.error("Error processing redis message: {}", payload, e));
                }, 3)
                .subscribe();
    }
}
