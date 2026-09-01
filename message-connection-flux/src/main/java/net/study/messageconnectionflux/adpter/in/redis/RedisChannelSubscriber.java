package net.study.messageconnectionflux.adpter.in.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.adpter.in.kafka.RecordDispatcher;
import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.config.PodIdentity;
import net.study.messageconnectionflux.util.JsonUtil;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
                redisOperations.<String, String>opsForStream();

        streamOps.createGroup(channel, ReadOffset.from("0"), CONSUMER_GROUP)
                .onErrorResume(e -> Mono.empty())
                .thenMany(
                        streamOps.read(
                                Consumer.from(CONSUMER_GROUP, consumerName),
                                StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                                StreamOffset.create(channel, ReadOffset.lastConsumed())
                        ).repeat()
                )
                .flatMap(record -> {
                    String payload = record.getValue().get("payload");
                    log.info("received message from stream: {} recordId: {} payload: {}", channel, record.getId(), payload);

                    return jsonUtil.fromJson(payload, RecordInterface.class)
                            .flatMap(recordInterface -> Mono.fromRunnable(() -> recordDispatcher.dispatch(recordInterface)))
                            .then(streamOps.acknowledge(CONSUMER_GROUP, record))
                            .doOnError(e -> log.error("Error processing stream message: {}", payload, e));
                }, 3)
                .subscribe();
    }
}
