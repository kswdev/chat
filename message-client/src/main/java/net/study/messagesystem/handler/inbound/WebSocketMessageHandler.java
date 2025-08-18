package net.study.messagesystem.handler.inbound;

import jakarta.websocket.MessageHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebSocketMessageHandler implements MessageHandler.Whole<String> {

    private final InboundMessageHandler inboundMessageHandler;

    @Override
    public void onMessage(String payload) {
        inboundMessageHandler.handle(payload);
    }
}
