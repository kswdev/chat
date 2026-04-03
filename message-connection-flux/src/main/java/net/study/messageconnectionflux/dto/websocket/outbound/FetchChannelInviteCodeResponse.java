package net.study.messageconnectionflux.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagecommon.constant.MessageType;
import net.study.messageconnectionflux.domain.channel.ChannelId;
import net.study.messageconnectionflux.domain.user.InviteCode;

@Getter
public class FetchChannelInviteCodeResponse extends BaseMessage {

    private final ChannelId channelId;
    private final InviteCode inviteCode;

    public FetchChannelInviteCodeResponse(ChannelId channelId, InviteCode inviteCode) {
        super(MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE);
        this.channelId = channelId;
        this.inviteCode = inviteCode;
    }
}
