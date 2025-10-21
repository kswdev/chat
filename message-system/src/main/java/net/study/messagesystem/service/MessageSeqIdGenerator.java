package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.domain.channel.ChannelId;
import net.study.messagesystem.domain.message.MessageSeqId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSeqIdGenerator {

    private final StringRedisTemplate redisTemplate;

    public Optional<MessageSeqId> getNext(ChannelId channelId) {
        String key = buildMessageSeqIdKey(channelId.id());

        try {
            return Optional.of(new MessageSeqId(redisTemplate.opsForValue().increment(key)));
        } catch (Exception e) {
            log.error("Redis get failed. key: {}, cause: {}", key, e.getMessage());
        }

        return Optional.empty();
    }

    private String buildMessageSeqIdKey(Long channelId) {
        return "%s:%d:seq_id".formatted(KeyPrefix.CHANNEL, channelId);
    }
}
