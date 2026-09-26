package net.study.messageconnectionflux.adpter.in.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.adpter.in.kafka.RecordDispatcher;
import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.config.PodIdentity;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.nio.ByteBuffer;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisChannelSubscriber {

    private final ReactiveRedisOperations<String, String> redisOperations;
    private final PodIdentity podIdentity;
    private final RecordDispatcher recordDispatcher;
    private final JsonUtil jsonUtil;

    private static final String CONSUMER_GROUP = "flux-consumer-group";
    private static final Duration RETRY_MIN_BACKOFF = Duration.ofSeconds(1);
    private static final Duration RETRY_MAX_BACKOFF = Duration.ofSeconds(30);

    @PostConstruct
    public void subscribe() {
        String channel = podIdentity.getDeliveryChannel();
        String consumerName = podIdentity.getPodName();

        ReactiveStreamOperations<String, String, String> streamOps =
                redisOperations.opsForStream();

        ensureConsumerGroupExists(channel)
                .thenMany(readStream(streamOps, channel, consumerName))
                .doOnSubscribe(s -> log.info("Subscribed to Redis stream: {} as consumer: {}", channel, consumerName))
                .flatMap(mapRecord -> handleRecord(streamOps, channel, mapRecord), 3)
                .retryWhen(Retry.backoff(Long.MAX_VALUE, RETRY_MIN_BACKOFF)
                        .maxBackoff(RETRY_MAX_BACKOFF)
                        .doBeforeRetry(signal -> log.error(
                                "Redis stream subscription failed, retrying. channel={} consumer={} attempt={}",
                                channel, consumerName, signal.totalRetries() + 1, signal.failure())))
                .subscribe(
                        v -> { },
                        error -> log.error(
                                "Redis stream subscription terminated permanently. channel={} consumer={}",
                                channel, consumerName, error));
    }

    private Mono<String> ensureConsumerGroupExists(String channel) {
        return redisOperations.execute(connection ->
                        connection.streamCommands().xGroupCreate(
                                ByteBuffer.wrap(channel.getBytes()),
                                CONSUMER_GROUP,
                                ReadOffset.from("0"),
                                true   // MK_STREAM
                        ))
                .next()
                .onErrorResume(this::isBusyGroup, e -> Mono.empty());
    }

    private boolean isBusyGroup(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains("BUSYGROUP")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private Flux<MapRecord<String, String, String>> readStream(
            ReactiveStreamOperations<String, String, String> streamOps,
            String channel, String consumerName
    ) {
        return streamOps.read(
                Consumer.from(CONSUMER_GROUP, consumerName),
                StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                StreamOffset.create(channel, ReadOffset.lastConsumed())
        ).repeat();
    }

    private Mono<Void> handleRecord(
            ReactiveStreamOperations<String, String, String> streamOps,
            String channel, MapRecord<String, String, String> mapRecord
    ) {
        String payload = mapRecord.getValue().get("payload");
        log.info("received message from stream: {} recordId: {} payload: {}", channel, mapRecord.getId(), payload);

        return jsonUtil.fromJson(payload, RecordInterface.class)
                .flatMap(recordInterface -> Mono.fromRunnable(() -> recordDispatcher.dispatch(recordInterface)))
                .then(streamOps.acknowledge(CONSUMER_GROUP, mapRecord))
                .then()
                .onErrorResume(e -> {
                    log.error("Error processing stream message, leaving unacked for retry/inspection. channel={} recordId={} payload={}",
                            channel, mapRecord.getId(), payload, e);
                    return Mono.empty();
                });
    }
}
