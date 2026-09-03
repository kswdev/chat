package net.study.messageconnectionflux.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamMetrics {

    private final ReactiveRedisOperations<String, String> redisOperations;
    private final PodIdentity podIdentity;
    private final MeterRegistry meterRegistry;
    private static final String CONSUMER_GROUP = "flux-consumer-group";

    /**
     * idle 시간 최대값을 구할 때 훑어볼 pending 메시지 개수.
     * pending 전량을 받아오면 백로그가 쌓였을 때 XPENDING 응답 자체가 부하가 되므로,
     * ID 오래된 순(= 배달 오래된 순) 상위 일부만 확인한다.
     */
    private static final int PENDING_SCAN_LIMIT = 64;

    private final AtomicLong pendingCount = new AtomicLong(0);
    private final AtomicLong maxIdleMillis = new AtomicLong(0);

    @PostConstruct
    public void registerGauge() {
        Gauge.builder("redis_stream_pending", pendingCount, AtomicLong::get)
                .description("이 pod의 stream에서 아직 ACK되지 않은 메시지 개수")
                .register(meterRegistry);

        Gauge.builder("redis_stream_pending_max_idle_ms", maxIdleMillis, AtomicLong::get)
                .description("가장 오래 밀려있는 메시지의 idle 시간(ms)")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 10000)
    public void refreshPendingCount() {
        String channel = podIdentity.getDeliveryChannel();

        ReactiveStreamOperations<String, String, String> streamOps =
                redisOperations.opsForStream();

        nullSafe(streamOps.pending(channel, CONSUMER_GROUP))
                .map(PendingMessagesSummary::getTotalPendingMessages)
                .transform(mono -> onNoGroupEmitZero(mono, channel))
                .defaultIfEmpty(0L)
                .subscribe(pendingCount::set,
                        error -> log.warn("stream pending 개수 조회 실패. channel={}", channel, error));

        nullSafe(streamOps.pending(channel, CONSUMER_GROUP, Range.unbounded(), PENDING_SCAN_LIMIT))
                .flatMap(pendingMessages -> Mono.justOrEmpty(
                        pendingMessages.stream()
                                .map(PendingMessage::getElapsedTimeSinceLastDelivery)
                                .max(Duration::compareTo)))
                .map(Duration::toMillis)
                .transform(mono -> onNoGroupEmitZero(mono, channel))
                .defaultIfEmpty(0L)
                .subscribe(maxIdleMillis::set,
                        error -> log.warn("stream pending idle 시간 조회 실패. channel={}", channel, error));
    }

    private static <T> Mono<T> nullSafe(@Nullable Mono<T> mono) {
        return mono != null ? mono : Mono.empty();
    }

    private static Mono<Long> onNoGroupEmitZero(Mono<Long> source, String channel) {
        return source.onErrorResume(
                error -> error.getMessage() != null && error.getMessage().contains("NOGROUP"),
                error -> {
                    log.debug("consumer group 미생성 상태. channel={}, group={}", channel, CONSUMER_GROUP);
                    return Mono.just(0L);
                });
    }
}
