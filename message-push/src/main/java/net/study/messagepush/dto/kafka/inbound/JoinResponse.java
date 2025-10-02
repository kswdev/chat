package net.study.messagepush.dto.kafka.inbound;

import net.study.messagepush.constant.MessageType;
import net.study.messagepush.dto.channel.ChannelId;
import net.study.messagepush.dto.user.UserId;

public record JoinResponse(UserId userId, ChannelId channelId, String title) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.JOIN_RESPONSE;
    }
}
