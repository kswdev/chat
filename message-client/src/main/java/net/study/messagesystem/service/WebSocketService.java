package net.study.messagesystem.service;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import net.study.messagesystem.dto.domain.Message;
import net.study.messagesystem.handler.WebSocketMessageHandler;
import net.study.messagesystem.handler.WebSocketSender;
import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;

public class WebSocketService {

    private final TerminalService terminalService;
    private final WebSocketSender webSocketSender;
    private final String websocketUrl;

    private WebSocketMessageHandler webSocketMessageHandler;
    private Session session;

    public WebSocketService(TerminalService terminalService, WebSocketSender webSocketSender, String websocketUrl, String endpoint) {
        this.terminalService = terminalService;
        this.webSocketSender = webSocketSender;
        this.websocketUrl = "ws://" + websocketUrl + endpoint;
    }

    public void setWebSocketMessageHandler(WebSocketMessageHandler webSocketMessageHandler) {
        this.webSocketMessageHandler = webSocketMessageHandler;
    }

    public boolean createSession() {
        ClientManager clientManager = ClientManager.createClient();
        try {
            session = clientManager.connectToServer(new WebSocketMessageHandler(terminalService), new URI(websocketUrl));
            session.addMessageHandler(webSocketMessageHandler);
            return true;
        } catch (Exception e) {
            terminalService.printSystemMessage(String.format("Failed to connect to [%s]: error: %s ", websocketUrl, e.getMessage()));
            return false;
        }
    }

    public void closeSession() {
        try {
            if (session != null && session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal close"));
            }
            session = null;
        } catch (IOException e) {
            terminalService.printSystemMessage(String.format("Failed to close error: %s ", e.getMessage()));
        }
    }

    public void sendMessage(Message message) {
        if (session != null && session.isOpen()) {
            webSocketSender.sendMessage(session, message);
        } else {
            terminalService.printSystemMessage("WebSocket is not connected.");
        }
    }
}
