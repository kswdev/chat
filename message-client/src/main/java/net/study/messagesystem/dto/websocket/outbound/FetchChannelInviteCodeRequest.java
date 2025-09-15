package net.study.messagesystem.dto.websocket.outbound;

import lombok.Getter;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.channel.ChannelId;

@Getter
public class FetchChannelInviteCodeRequest extends BaseRequest {

    private final ChannelId channelId;

    public FetchChannelInviteCodeRequest(
            ChannelId ChannelId
    ) {
        super(MessageType.FETCH_CHANNEL_INVITE_CODE_REQUEST);
        this.channelId = ChannelId;
    }
}
