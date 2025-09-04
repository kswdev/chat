package net.study.messagesystem.handler.inbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.websocket.inbound.*;
import net.study.messagesystem.service.TerminalService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ResponseDispatcher {

    private final TerminalService terminalService;
    private final Map<String, Consumer<BaseMessage>> handlers = new HashMap<>();

    public ResponseDispatcher(TerminalService terminalService) {
        this.terminalService = terminalService;
        preparedHandlers();
    }

    private void preparedHandlers() {
        handlers.put(MessageType.ASK_INVITE, (msg) -> askInvite((InviteNotification) msg));
        handlers.put(MessageType.ENTER_RESPONSE, (msg) -> enter((EnterResponse) msg));
        handlers.put(MessageType.CREATE_RESPONSE, (msg) -> create((CreateResponse) msg));
        handlers.put(MessageType.INVITE_RESPONSE, (msg) -> invite((InviteResponse) msg));
        handlers.put(MessageType.ACCEPT_RESPONSE, (msg) -> accept((AcceptResponse) msg));
        handlers.put(MessageType.REJECT_RESPONSE, (msg) -> reject((RejectResponse) msg));
        handlers.put(MessageType.DISCONNECT_RESPONSE, (msg) -> disconnect((DisconnectResponse) msg));
        handlers.put(MessageType.NOTIFY_ACCEPT, (msg) -> acceptNotification((AcceptNotification) msg));
        handlers.put(MessageType.NOTIFY_JOIN, (msg) -> joinChannelNotification((JoinNotification) msg));
        handlers.put(MessageType.NOTIFY_MESSAGE, (msg) -> message((MessageNotification) msg));
        handlers.put(MessageType.FETCH_USER_CONNECTIONS_RESPONSE, (msg) -> connections((FetchUserConnectionsResponse) msg));
        handlers.put(MessageType.FETCH_USER_INVITE_CODE_RESPONSE, (msg) -> fetchUserInviteCode((FetchUserInviteCodeResponse) msg));

        handlers.put(MessageType.ERROR, (msg) -> error((ErrorResponse) msg));
    }

    public void dispatch(BaseMessage message) {
        handlers.get(message.getType())
                .accept(message);
    }

    private void askInvite(InviteNotification inviteNotification) {
        terminalService.printSystemMessage("Do you want to accept invite from %s?".formatted(inviteNotification.getUsername()));
    }

    private void enter(EnterResponse enterResponse) {
        terminalService.printSystemMessage("title: %s, channelId: %s".formatted(enterResponse.getTitle(), enterResponse.getChannelId().id()));
    }

    private void create(CreateResponse createResponse) {
        terminalService.printSystemMessage("title: %s, channelId: %s".formatted(createResponse.getTitle(), createResponse.getChannelId().id()));
    }

    private void invite(InviteResponse inviteResponse) {
        terminalService.printSystemMessage("Invite: %s, result: %s".formatted(inviteResponse.getInviteCode().code(), inviteResponse.getStatus()));
    }

    private void accept(AcceptResponse acceptResponse) {
        terminalService.printSystemMessage("Connect %s".formatted(acceptResponse.getUsername()));
    }

    private void reject(RejectResponse rejectResponse) {
        terminalService.printSystemMessage("Reject: %s, result: %s".formatted(rejectResponse.getUsername(), rejectResponse.getStatus()));
    }

    private void disconnect(DisconnectResponse disconnectResponse) {
        terminalService.printSystemMessage("Disconnect: %s, result: %s".formatted(disconnectResponse.getUsername(), disconnectResponse.getStatus()));
    }

    private void acceptNotification(AcceptNotification acceptNotification) {
        terminalService.printSystemMessage("%s accept your invite".formatted(acceptNotification.getUsername()));
    }

    private void joinChannelNotification(JoinNotification joinNotification) {
        terminalService.printSystemMessage("Join channel. title: %s, channelId: %s".formatted(joinNotification.getTitle(), joinNotification.getChannelId().id()));
    }

    private void message(MessageNotification messageNotification) {
        terminalService.printMessage(messageNotification.getUsername(), messageNotification.getContent());
    }

    private void fetchUserInviteCode(FetchUserInviteCodeResponse inviteCodeResponse) {
        terminalService.printSystemMessage("My invite code: %s".formatted(inviteCodeResponse.getInviteCode().code()));
    }

    private void connections(FetchUserConnectionsResponse connectionsResponse) {
        connectionsResponse
                .getConnections()
                .forEach(connection -> terminalService.printSystemMessage(" %s - %s".formatted(connection.username(), connection.status())));
    }

    private void error(ErrorResponse errorResponse) {
        terminalService.printSystemMessage("Error at %s, cause %s".formatted(errorResponse.getMessageType(), errorResponse.getMessage()));
    }
}
