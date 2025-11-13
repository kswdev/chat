package net.study.messageconnection.dto.kafka;

import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.user.InviteCode;
import net.study.messageconnection.domain.user.UserId;


public record FetchChannelInviteCodeResponseRecord(UserId userId, ChannelId channelId, InviteCode inviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE;
    }
}