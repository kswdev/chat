package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.Channel;
import net.study.messageconnection.domain.user.UserId;

import java.util.List;


public record FetchChannelsResponseRecord(UserId userId, List<Channel> channels) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNELS_RESPONSE;
    }
}