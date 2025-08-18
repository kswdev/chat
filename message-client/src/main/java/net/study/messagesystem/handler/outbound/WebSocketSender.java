package net.study.messagesystem.handler.outbound;

import jakarta.websocket.SendHandler;
import jakarta.websocket.Session;
import net.study.messagesystem.dto.websocket.outbound.BaseRequest;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.util.JsonUtil;

public class WebSocketSender {

    private final TerminalService terminalService;

    public WebSocketSender(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    public void sendMessage(Session session, BaseRequest request) {
        if (session != null && session.isOpen()) {
            JsonUtil.toJson(request)
                    .ifPresent(payload -> session.getAsyncRemote().sendText(payload, failureLoggingHandler(payload)));
        }
    }

    private SendHandler failureLoggingHandler(String payload) {
        return result -> {
            if (!result.isOK()) {
                terminalService.printSystemMessage("%s Failed to send message. error: %s ".formatted(payload, result.getException()));
            }
        };
    }
}
