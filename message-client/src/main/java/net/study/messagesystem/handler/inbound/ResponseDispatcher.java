package net.study.messagesystem.handler.inbound;

import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.dto.websocket.inbound.BaseMessage;
import net.study.messagesystem.dto.websocket.inbound.InviteResponse;
import net.study.messagesystem.dto.websocket.inbound.MessageNotification;
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
        handlers.put(MessageType.NOTIFY_MESSAGE, (msg) -> message((MessageNotification) msg));
        handlers.put(MessageType.FETCH_USER_INVITE_CODE_RESPONSE, (msg) -> inviteCode((InviteResponse) msg));
    }

    public void dispatch(BaseMessage message) {
        handlers.get(message.getType())
                .accept(message);
    }

    private void message(MessageNotification messageNotification) {
        terminalService.printMessage(messageNotification.getUsername(), messageNotification.getContent());
    }

    private void inviteCode(InviteResponse inviteResponse) {
        terminalService.printSystemMessage("My invite code: %s".formatted(inviteResponse.getInviteCode().code()));
    }
}
