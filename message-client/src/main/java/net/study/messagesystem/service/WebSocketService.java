package net.study.messagesystem.service;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.SendHandler;
import jakarta.websocket.Session;
import net.study.messagesystem.dto.websocket.outbound.BaseRequest;
import net.study.messagesystem.dto.websocket.outbound.KeepAliveRequest;
import net.study.messagesystem.dto.websocket.outbound.WriteMessage;
import net.study.messagesystem.handler.inbound.WebSocketMessageHandler;
import net.study.messagesystem.handler.session.WebSocketSessionHandler;
import net.study.messagesystem.util.JsonUtil;
import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketService {


    private final String websocketUrl;
    private final UserService userService;
    private final TerminalService terminalService;
    private final MessageService messageService;
    private final WebSocketMessageHandler webSocketMessageHandler;


    private Session session;
    private ScheduledExecutorService executorService;

    public WebSocketService(
            String websocketUrl, String endpoint,
            UserService userService,
            TerminalService terminalService,
            MessageService messageService,
            WebSocketMessageHandler webSocketMessageHandler

    ) {
        this.websocketUrl = "ws://" + websocketUrl + endpoint;
        this.userService = userService;
        this.terminalService = terminalService;
        this.messageService = messageService;
        this.webSocketMessageHandler = webSocketMessageHandler;
    }

    public boolean createSession(String sessionId) {
        ClientManager clientManager = ClientManager.createClient();
        ClientEndpointConfig config = createClientEndpointConfig(sessionId);
        
        try {
            session = clientManager.connectToServer(
                    new WebSocketSessionHandler(userService, terminalService, this), config, new URI(websocketUrl));
            session.addMessageHandler(webSocketMessageHandler);
            enableKeepAlive();
            return true;
        } catch (Exception e) {
            terminalService.printSystemMessage(String.format("Failed to connect to [%s]: error: %s ", websocketUrl, e.getMessage()));
            return false;
        }
    }

    private static ClientEndpointConfig createClientEndpointConfig(String sessionId) {
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {

            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Cookie", List.of("SESSION=" + sessionId));
            }
        };

        return ClientEndpointConfig.Builder
                .create().configurator(configurator).build();
    }

    public boolean closeSession() {
        try {
            disableKeepAlive();

            if (isSessionOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Normal close"));
            }
            session = null;

            return true;
        } catch (IOException e) {
            terminalService.printSystemMessage(String.format("Failed to close error: %s ", e.getMessage()));
            return false;
        }
    }

    public void sendMessage(BaseRequest baseRequest) {
        if (isSessionOpen()) {
            if (baseRequest instanceof WriteMessage messageRequest) {
                messageService.sendMessage(session, messageRequest);
                return;
            }

            JsonUtil.toJson(baseRequest)
                    .ifPresent(payload -> session.getAsyncRemote().sendText(payload, failureLoggingHandler(payload)));
        } else {
            terminalService.printSystemMessage("WebSocket is not connected.");
        }
    }

    private void enableKeepAlive() {
        if (executorService == null)
            executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(() -> sendMessage(new KeepAliveRequest()), 1, 1, TimeUnit.MINUTES);
    }

    private void disableKeepAlive() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private boolean isSessionOpen() {
        return session != null && session.isOpen();
    }

    private SendHandler failureLoggingHandler(String payload) {
        return result -> {
            if (!result.isOK()) {
                terminalService.printSystemMessage("%s Failed to send message. error: %s ".formatted(payload, result.getException()));
            }
        };
    }
}
