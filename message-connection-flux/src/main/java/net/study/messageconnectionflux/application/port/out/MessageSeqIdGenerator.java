package net.study.messageconnectionflux.application.port.out;

import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.message.MessageSeqId;
import reactor.core.publisher.Mono;

public interface MessageSeqIdGenerator {
    Mono<MessageSeqId> getNextMessageSeqId(ChannelId channelId);
}
