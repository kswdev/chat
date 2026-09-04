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
                .subscribe();
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
        return e.getMessage().contains("BUSYGROUP");
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
                .doOnError(e -> log.error("Error processing stream message: {}", payload, e))
                .then();
    }
}
