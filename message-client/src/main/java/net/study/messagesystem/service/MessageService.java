package net.study.messagesystem.service;

import jakarta.websocket.SendHandler;
import jakarta.websocket.Session;
import lombok.Setter;
import net.study.messagesystem.dto.message.Message;
import net.study.messagesystem.dto.message.MessageSeqId;
import net.study.messagesystem.dto.websocket.inbound.FetchMessagesResponse;
import net.study.messagesystem.dto.websocket.inbound.MessageNotification;
import net.study.messagesystem.dto.websocket.inbound.WriteMessageAck;
import net.study.messagesystem.dto.websocket.outbound.FetchMessagesRequest;
import net.study.messagesystem.dto.websocket.outbound.ReadMessageAck;
import net.study.messagesystem.dto.websocket.outbound.WriteMessage;
import net.study.messagesystem.util.JsonUtil;

import java.util.Map;
import java.util.concurrent.*;

public class MessageService {

    private final int LIMIT_RETRIES = 5;
    private final long TIMEOUT_MS = 1000L;

    @Setter
    private WebSocketService webSocketService;

    private final UserService userService;
    private final TerminalService terminalService;
    private final Map<Long, CompletableFuture<WriteMessageAck>> pendingMessages = new ConcurrentHashMap<>();
    private final Map<MessageSeqIdRange, ScheduledFuture<?>> scheduledFetchMessagesRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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

    public void sendMessage(FetchMessagesRequest fetchMessagesRequest) {
        webSocketService.sendMessage(fetchMessagesRequest);
    }

    public void sendMessage(Session session, WriteMessage message) {
        sendMessage(session, message, 5);
    }

    public void receiveMessage(FetchMessagesResponse fetchMessagesResponse) {
        if (userService.isInLobby() || !userService.getChannelId().equals(fetchMessagesResponse.getChannelId())) {
            terminalService.printSystemMessage("Invalid channel id ignore fetch messages");
            return;
        }

        fetchMessagesResponse.getMessages().forEach(userService::addMessage);
        processMessageBuffer();
    }

    public void receiveMessage(MessageNotification messageNotification) {
        if (userService.isInLobby() || !userService.getChannelId().equals(messageNotification.getChannelId())) {
            terminalService.printSystemMessage("Invalid channel id. ignore message");
            return;
        }
        MessageSeqId lastReadMessageSeqId = userService.getLastReadMessageSeqId();
        MessageSeqId receivedMessageSeqId = messageNotification.getMessageSeqId();

        // 마지막 읽은 메시지가 없거나, 받은 메시지가 바로 다음 순서인 경우
        if (isInitOrNextMessage(lastReadMessageSeqId, receivedMessageSeqId)) {
            for (MessageSeqIdRange idRange : scheduledFetchMessagesRequests.keySet()) {

                // 범위 확인: 받은 메시지 ID가 예약된 범위 안에 있는지 확인
                if (receivedMessageSeqId.id() >= idRange.startSeqId.id() && receivedMessageSeqId.id() <= idRange.endSeqId.id()) {

                    // 요청 취소: 해당하는 스케줄된 요청을 취소하고 맵에서 제거
                    scheduledFetchMessagesRequests.get(idRange).cancel(false);
                    scheduledFetchMessagesRequests.remove(idRange);

                    // 범위 재조정: 범위가 단일 메시지가 아니면, 다시 예약 (받은 메시지를 제외한 나머지 범위)
                    // 받은 메시지(7) 이후 범위: [8, 10]
                    if (receivedMessageSeqId.id() < idRange.endSeqId.id()) {
                        reserveFetchMessagesRequest(new MessageSeqId(receivedMessageSeqId.id() + 1), idRange.endSeqId);
                    }
                }
            }

            userService.addMessage(new Message(
                    messageNotification.getChannelId(),
                    messageNotification.getMessageSeqId(),
                    messageNotification.getUsername(),
                    messageNotification.getContent()));

            processMessageBuffer();

        // 조건: 받은 메시지 ID가 마지막 읽은 메시지 ID + 1보다 큰 경우 (중간에 누락된 메시지 존재)
        } else if (isOmissionMessage(lastReadMessageSeqId, receivedMessageSeqId)) {
            // 일단 받은 메세지 버퍼에 추가
            userService.addMessage(new Message(
                    messageNotification.getChannelId(),
                    messageNotification.getMessageSeqId(),
                    messageNotification.getUsername(),
                    messageNotification.getContent()));

            // 새로 메세지 범위 정하여 다시 예약
            reserveFetchMessagesRequest(
                    new MessageSeqId(lastReadMessageSeqId.id()+1),
                    new MessageSeqId(receivedMessageSeqId.id()-1));
        } else if (isAlreadyReadMessage(lastReadMessageSeqId, receivedMessageSeqId)) {
            terminalService.printSystemMessage("Ignore duplicate message id: " + messageNotification.getMessageSeqId());
        }
    }

    private void processMessageBuffer() {
        while (!userService.isBufferEmpty()) {
            MessageSeqId peekedMessageSeqId = userService.peekMessage().messageSeqId();
            MessageSeqId lastReadMessageSeqId = userService.getLastReadMessageSeqId();

            if (isInitOrNextMessage(lastReadMessageSeqId, peekedMessageSeqId)) {
                Message message = userService.popMessage();
                userService.setLastReadMessageSeqId(message.messageSeqId());
                webSocketService.sendMessage(new ReadMessageAck(userService.getChannelId(), message.messageSeqId()));
                terminalService.printMessage(message.username(), message.content());
            } else if (isAlreadyReadMessage(lastReadMessageSeqId, peekedMessageSeqId)) {
                userService.popMessage();
            } else if (isOmissionMessage(lastReadMessageSeqId, peekedMessageSeqId)) {
                break;
            }
        }
    }

    private void reserveFetchMessagesRequest(MessageSeqId start, MessageSeqId end) {
        MessageSeqIdRange messageSeqIdRange = new MessageSeqIdRange(new MessageSeqId(start.id()), new MessageSeqId(end.id()));
        ScheduledFuture<?> scheduledFuture = scheduler.schedule(() -> {
            webSocketService.sendMessage(new FetchMessagesRequest(userService.getChannelId(), messageSeqIdRange.startSeqId(), messageSeqIdRange.endSeqId()));
            scheduledFetchMessagesRequests.remove(messageSeqIdRange);
        }, 100, TimeUnit.MILLISECONDS);
        scheduledFetchMessagesRequests.put(messageSeqIdRange, scheduledFuture);
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

    private boolean isInitOrNextMessage(MessageSeqId currentMessageSeq, MessageSeqId peekedMessageSeq) {
        return currentMessageSeq == null || currentMessageSeq.id() + 1 == peekedMessageSeq.id();
    }

    private boolean isAlreadyReadMessage(MessageSeqId currentMessageSeq, MessageSeqId peekedMessageSeq) {
        return currentMessageSeq.id() >= peekedMessageSeq.id();
    }

    private boolean isOmissionMessage(MessageSeqId currentMessageSeq, MessageSeqId peekedMessageSeq) {
        return currentMessageSeq.id() + 1 < peekedMessageSeq.id();
    }

    private record MessageSeqIdRange(MessageSeqId startSeqId, MessageSeqId endSeqId) { }
}
