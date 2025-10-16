package net.study.messageauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheService {
    private final StringRedisTemplate stringRedisTemplate;

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
