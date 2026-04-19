package net.study.messageconnectionflux.adpter.out.persistence.redis.message;

import lombok.RequiredArgsConstructor;
import net.study.messageconnectionflux.application.port.out.MessageSeqIdGenerator;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.MessageSeqId;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageSeqIdGenerator {

    private final MessageRepository messageRepository;

    public Mono<MessageSeqId> getNextMessageSeqId(ChannelId channelId) {
        return messageRepository
                .getNextMessageSequence(channelId.id())
                .map(MessageSeqId::new);
    }
}
