package net.study.messagesystem.dto.websocket.inbound;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;
import net.study.messagesystem.dto.user.InviteCode;

@Getter
public class FetchChannelInviteCodeResponse extends BaseMessage{

    private final ChannelId channelId;
    private final InviteCode inviteCode;

    @JsonCreator
    public FetchChannelInviteCodeResponse(
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("inviteCode") InviteCode inviteCode
    ) {
        super(MessageType.FETCH_CHANNEL_INVITE_CODE_RESPONSE);
        this.channelId = channelId;
        this.inviteCode = inviteCode;
    }
}
