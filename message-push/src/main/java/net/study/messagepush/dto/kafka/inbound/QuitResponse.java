package net.study.messagepush.dto.kafka.inbound;

import net.study.messagecommon.constant.MessageType;
import net.study.messagepush.dto.channel.ChannelId;
import net.study.messagepush.dto.user.UserId;

public record QuitResponse(UserId userId, ChannelId channelId) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.QUIT_RESPONSE;
    }
}
