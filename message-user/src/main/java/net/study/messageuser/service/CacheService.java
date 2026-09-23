package net.study.messageuser.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheService {
    private final StringRedisTemplate stringRedisTemplate;

    public Optional<String> get(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null)
                return Optional.of(value);
        } catch (Exception e) {
            log.error("Redis get failed. key: {}", key);
        }
        return Optional.empty();
    }

    public boolean set(String key, String value, long ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("Redis set failed. key: {}", key);
        }
        return false;
    }

    public boolean delete(Collection<String> keys) {
        try {
            stringRedisTemplate.delete(keys);
            return true;
        } catch (Exception e) {
            log.error("Redis multi delete failed. keys: {}", keys);
        }
        return false;
    }

    public String buildKey(String prefix, String key) {
        return "%s:%s".formatted(prefix, key);
    }
}
