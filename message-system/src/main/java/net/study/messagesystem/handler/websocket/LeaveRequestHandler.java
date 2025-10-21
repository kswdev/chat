package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.IdKey;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.LeaveRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.LeaveResponse;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveRequestHandler implements BaseRequestHandler<LeaveRequest> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRequest(WebSocketSession senderSession, LeaveRequest request) {
        UserId userId = (UserId) senderSession.getAttributes().get(IdKey.USER_ID.getValue());

        if(channelService.leave(userId))
            clientNotificationService.sendMessage(senderSession, userId, new LeaveResponse());
        else
            clientNotificationService.sendMessage(senderSession, userId, new ErrorResponse(ResultType.FAILED.getMessage(), MessageType.LEAVE_REQUEST));
    }

    @Override
    public Class<LeaveRequest> getRequestType() {
        return LeaveRequest.class;
    }
}
