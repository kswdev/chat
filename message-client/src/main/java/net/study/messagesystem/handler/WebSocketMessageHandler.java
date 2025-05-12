package net.study.messagesystem.handler;

import jakarta.websocket.MessageHandler;
import net.study.messagesystem.dto.Message;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.util.JsonUtil;

public class WebSocketMessageHandler implements MessageHandler.Whole<String> {

    private final TerminalService terminalService;

    public WebSocketMessageHandler(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @Override
    public void onMessage(String payload) {
        JsonUtil.fromJson(payload, Message.class)
                .ifPresent(message -> terminalService.printMessage(message.username(), message.content()));
    }
}
