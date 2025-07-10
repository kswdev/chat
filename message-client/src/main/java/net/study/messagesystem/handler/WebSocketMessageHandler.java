package net.study.messagesystem.handler;

import jakarta.websocket.MessageHandler;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.dto.domain.Message;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.util.JsonUtil;

@RequiredArgsConstructor
public class WebSocketMessageHandler implements MessageHandler.Whole<String> {

    private final TerminalService terminalService;

    @Override
    public void onMessage(String payload) {
        JsonUtil.fromJson(payload, Message.class)
                .ifPresent(message -> terminalService.printMessage(message.username(), message.content()));
    }
}
