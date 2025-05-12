package net.study.messagesystem.handler;

import jakarta.websocket.Session;
import net.study.messagesystem.dto.Message;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.util.JsonUtil;

public class WebSocketSender {

    private final TerminalService terminalService;

    public WebSocketSender(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    public void sendMessage(Session session, Message message) {
        if (session != null && session.isOpen()) {
            JsonUtil.toJson(message)
                    .ifPresent(msg -> {
                        try {
                            session.getBasicRemote().sendText(msg);
                        } catch (Exception e) {
                            terminalService.printSystemMessage(
                                    String.format("%s Failed to send message. error: %s ", msg, e.getMessage()));
                        }
                    });
        }
    }
}
