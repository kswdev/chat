package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.Constants;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.websocket.inbound.FetchUserInviteCodeRequest;
import net.study.messagesystem.dto.websocket.outbound.ErrorResponse;
import net.study.messagesystem.dto.websocket.outbound.FetchUserInviteCodeResponse;
import net.study.messagesystem.service.UserService;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUserInviteCodeRequestHandler implements BaseRequestHandler<FetchUserInviteCodeRequest> {

    private final UserService userService;
    private final WebSocketSessionManager webSocketSessionManager;

    @Override
    public void handleRequest(WebSocketSession senderSession, FetchUserInviteCodeRequest request) {
        UserId requestUserId = (UserId) senderSession.getAttributes().get(Constants.USER_ID.getValue());

        userService.getInviteCode(requestUserId)
                   .ifPresentOrElse(
                           inviteCode ->
                               webSocketSessionManager.sendMessage(senderSession, new FetchUserInviteCodeResponse(inviteCode))
                           ,() ->
                               webSocketSessionManager.sendMessage(senderSession, new ErrorResponse(MessageType.FETCH_USER_INVITE_CODE_REQUEST, "fetch user invite code failed.")));
    }
    @Override
    public Class<FetchUserInviteCodeRequest> getRequestType() {
        return FetchUserInviteCodeRequest.class;
    }
}
