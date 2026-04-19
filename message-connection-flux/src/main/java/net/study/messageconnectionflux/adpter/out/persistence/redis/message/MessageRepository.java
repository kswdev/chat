package net.study.messageconnectionflux.adpter.out.persistence.redis.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagecommon.constant.KeyPrefix;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Repository
public class MessageRepository {

    private final ReactiveRedisOperations<String, String> redisOperations;

    public Mono<Long> getNextMessageSequence(Long channelId) {
        String key = buildMessageSeqIdKey(channelId);
        return redisOperations.opsForValue()
                .increment(key)
                .doOnError((e) -> log.info("Redis get failed. key: {}, cause: {}", key, e.getMessage()))
                .onErrorResume(__ -> Mono.just(1L));
    }

    private String buildMessageSeqIdKey(Long channelId) {
        return "%s:%d:seq_id".formatted(KeyPrefix.CHANNEL, channelId);
    }
}

