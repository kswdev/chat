package net.study.messagesystem.handler.inbound;

import jakarta.websocket.MessageHandler;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.dto.websocket.inbound.BaseMessage;
import net.study.messagesystem.util.JsonUtil;

@RequiredArgsConstructor
public class WebSocketMessageHandler implements MessageHandler.Whole<String> {

    private final ResponseDispatcher responseDispatcher;

    @Override
    public void onMessage(String payload) {
        JsonUtil.fromJson(payload, BaseMessage.class)
                .ifPresent(responseDispatcher::dispatch);

    }
}
