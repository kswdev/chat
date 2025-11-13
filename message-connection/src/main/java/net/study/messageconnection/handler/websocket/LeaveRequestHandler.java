package net.study.messageconnection.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.constant.IdKey;
import net.study.messageconnection.constant.MessageType;
import net.study.messageconnection.constant.ResultType;
import net.study.messageconnection.domain.user.UserId;
import net.study.messageconnection.dto.websocket.inbound.LeaveRequest;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.dto.websocket.outbound.LeaveResponse;
import net.study.messageconnection.service.ChannelService;
import net.study.messageconnection.service.ClientNotificationService;
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
            clientNotificationService.sendError(senderSession, userId, new LeaveResponse());
        else
            clientNotificationService.sendError(senderSession, userId, new ErrorResponse(ResultType.FAILED.getMessage(), MessageType.LEAVE_REQUEST));
    }

    @Override
    public Class<LeaveRequest> getRequestType() {
        return LeaveRequest.class;
    }
}
