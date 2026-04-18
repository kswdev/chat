package net.study.messageconnectionflux.adpter.out.persistence.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.port.out.CachePort;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisCacheRepository implements CachePort {
    private final ReactiveRedisOperations<String, String> redisOperations;

    public Mono<String> get(String key) {
        return redisOperations
                .opsForValue().get(key)
                .doOnError(__ -> log.error("Redis get failed. key: {}", key))
                .onErrorResume(__ -> Mono.empty());
    }

    public Mono<List<String>> get(Collection<String> keys) {
        return redisOperations
                .opsForValue().multiGet(keys)
                .doOnError(__ -> log.error("Redis get failed. key: {}", keys))
                .onErrorResume(__ -> Mono.just(Collections.emptyList()));
    }

    public Mono<Boolean> set(String key, String value, long ttl) {
        return redisOperations.opsForValue()
                .set(key, value, Duration.ofSeconds(ttl))
                .doOnError(e -> log.error("Redis set failed. key: {}", key))
                .onErrorReturn(false);
    }

    public Mono<Boolean> set(Map<String, String> map, long ttl) {
        return Flux.fromIterable(map.entrySet())
                .flatMap(entry -> set(entry.getKey(), entry.getValue(), ttl))
                .reduce(Boolean::logicalAnd)
                .doOnError(e -> log.error("Redis multi set failed. keys: {}, cause: {}", map.keySet(), e.getMessage()))
                .onErrorReturn(false);
    }

    public Mono<Boolean> expire(String key, long ttl) {
        return redisOperations
                .expire(key, Duration.ofSeconds(ttl))
                .doOnError(e -> log.error("Redis expire failed. key: {}", key))
                .onErrorReturn(false);
    }

    public Mono<Long> delete(String key) {
        return redisOperations
                .delete(key)
                .doOnError(e -> log.error("Redis delete failed. key: {}", key))
                .onErrorReturn(0L);
    }

}
