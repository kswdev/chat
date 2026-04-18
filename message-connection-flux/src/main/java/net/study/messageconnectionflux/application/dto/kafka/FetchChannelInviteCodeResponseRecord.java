package net.study.messageconnectionflux.application.dto.kafka;

import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.user.InviteCode;
import net.study.messageconnectionflux.domain.user.UserId;


public record FetchChannelInviteCodeResponseRecord(UserId userId, ChannelId channelId, InviteCode inviteCode) implements RecordInterface {

    @Override
    public String type() {
        return MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE;
    }
}