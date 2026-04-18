package net.study.messageconnectionflux.application.port.out;

import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.user.UserId;
import reactor.core.publisher.Mono;

public interface EventProducer {
    Mono<Void> sendMessageUsingPartitionKey(
            ChannelId channelId,
            UserId userId,
            RecordInterface recordInterface,
            Runnable errorCallback
    );

    Mono<Void> sendRequest(RecordInterface recordInterface, Runnable errorCallback);
    Mono<Void> sendPushNotification(RecordInterface recordInterface);
}
