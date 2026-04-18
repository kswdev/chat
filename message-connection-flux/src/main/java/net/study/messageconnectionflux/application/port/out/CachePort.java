package net.study.messageconnectionflux.application.port.out;

import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CachePort {
    Mono<String> get(String key);
    Mono<List<String>> get(Collection<String> keys);
    Mono<Boolean> set(String key, String value, long ttl);
    Mono<Boolean> set(Map<String, String> map, long ttl);
    Mono<Boolean> expire(String key, long ttl);
    Mono<Long> delete(String key);
}
