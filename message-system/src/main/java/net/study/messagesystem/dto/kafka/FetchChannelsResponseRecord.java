package net.study.messagesystem.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messagesystem.domain.channel.Channel;
import net.study.messagesystem.domain.user.UserId;

import java.util.List;


public record FetchChannelsResponseRecord(UserId userId, List<Channel> channels) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNELS_RESPONSE;
    }
}