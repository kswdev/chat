package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.domain.channel.ChannelId;
import net.study.messageconnection.domain.user.InviteCode;

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
