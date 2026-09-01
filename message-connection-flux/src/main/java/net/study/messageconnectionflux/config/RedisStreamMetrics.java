package net.study.messageconnectionflux.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class RedisStreamMetrics {

    private final ReactiveRedisOperations<String, String> redisOperations;
    private final PodIdentity podIdentity;
    private final MeterRegistry meterRegistry;
    private static final String CONSUMER_GROUP = "flux-consumer-group";

    private final AtomicLong pendingCount = new AtomicLong(0);

    @PostConstruct
    public void registerGauge() {
        Gauge.builder("redis_stream_pending", pendingCount, AtomicLong::get)
                .description("이 pod의 stream에서 아직 ACK되지 않은 메시지 개수")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 10000)
    public void refreshPendingCount() {
        String channel = podIdentity.getDeliveryChannel();
        redisOperations.opsForStream()
                .pending(channel, CONSUMER_GROUP)
                .map(PendingMessagesSummary::getTotalPendingMessages)
                .defaultIfEmpty(0L)
                .subscribe(pendingCount::set);
    }
}
