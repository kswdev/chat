package net.study.messageconnection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
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

    public List<String> get(Collection<String> keys) {
        try {
            return stringRedisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            log.error("Redis multi get failed. keys: {}", keys);
        }
        return Collections.emptyList();
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

    public boolean set(Map<String, String> map, long ttl) {
        try {
            map.forEach((key, value) -> set(key, value, ttl));
            return true;
        } catch (Exception e) {
            log.error("Redis multi set failed. keys: {}, cause: {}", map.keySet(), e.getMessage());
        }
        return false;
    }

    public boolean expire(String key, long ttl) {
        try {
            return stringRedisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis expire failed. key: {}", key);
            return false;
        }
    }

    public boolean delete(String key) {
        try {
            stringRedisTemplate.delete(key);
            return true;
        } catch (Exception e) {
            log.error("Redis delete failed. key: {}", key);
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

    public String buildKey(String prefix, String firstKey, String secondKey) {
        return "%s:%s:%s".formatted(prefix, firstKey, secondKey);
    }
}
