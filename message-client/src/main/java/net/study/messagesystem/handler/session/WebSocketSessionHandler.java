package net.study.messagesystem.handler.session;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import net.study.messagesystem.service.TerminalService;
import net.study.messagesystem.service.WebSocketService;

@RequiredArgsConstructor
public class WebSocketSessionHandler extends Endpoint {

    private final TerminalService terminalService;
    private final WebSocketService webSocketService;

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        terminalService.printSystemMessage("WebSocket Connected.");
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        webSocketService.closeSession();
        terminalService.printSystemMessage("WebSocket Disconnected: " + closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable thr) {
        terminalService.printSystemMessage("WebSocket Error: " + thr.getMessage());
    }
}
