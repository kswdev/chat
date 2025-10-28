package net.study.messagesystem.service;

import jakarta.websocket.SendHandler;
import jakarta.websocket.Session;
import net.study.messagesystem.dto.websocket.inbound.WriteMessageAck;
import net.study.messagesystem.dto.websocket.outbound.WriteMessage;
import net.study.messagesystem.util.JsonUtil;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageService {

    private final int LIMIT_RETRIES = 5;
    private final long TIMEOUT_MS = 1000L;

    private final UserService userService;
    private final TerminalService terminalService;
    private final Map<Long, CompletableFuture<WriteMessageAck>> pendingMessages = new ConcurrentHashMap<>();

    public MessageService(UserService userService, TerminalService terminalService) {
        this.userService = userService;
        this.terminalService = terminalService;
    }

    public void receiveMessage(WriteMessageAck writeMessageAck) {
        CompletableFuture<WriteMessageAck> future = pendingMessages.get(writeMessageAck.getSerial());
        if (future != null) {
            future.complete(writeMessageAck);
        }
    }

    public void sendMessage(Session session, WriteMessage message) {
        sendMessage(session, message, 5);
    }

    private void sendMessage(Session session, WriteMessage message, int retries) {
        CompletableFuture<WriteMessageAck> future = new CompletableFuture<>();
        future.orTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .whenCompleteAsync((response, throwable) -> {
                    if (response != null) {
                        userService.setLastReadMessageSeqId(response.getMessageSeqId());
                        terminalService.printMessage("<me>", message.getContent());
                        pendingMessages.remove(message.getSerial());
                    } else if (throwable instanceof TimeoutException && retries < LIMIT_RETRIES) {
                        sendMessage(session, message, retries + 1);
                    } else {
                        terminalService.printSystemMessage("Failed to send message. error: %s".formatted(throwable.getMessage()));
                        pendingMessages.remove(message.getSerial());
                    }
                });

        pendingMessages.put(message.getSerial(), future);

        if (session != null && session.isOpen()) {
            JsonUtil.toJson(message)
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
