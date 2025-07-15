package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import net.study.messagesystem.dto.message.Message;
import net.study.messagesystem.dto.websocket.inbound.WriteMessageRequest;
import net.study.messagesystem.entity.messae.MessageEntity;
import net.study.messagesystem.repository.MessageRepository;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static java.util.function.Predicate.not;

@Component
@RequiredArgsConstructor
public class WriteMessageRequestHandler implements BaseRequestHandler<WriteMessageRequest> {

    private final WebSocketSessionManager sessionManager;
    private final MessageRepository messageRepository;

    @Override
    public void handleRequest(WebSocketSession senderSession, WriteMessageRequest request) {
        Message receivedMessage = new Message(request.getUsername(), request.getContent());
        messageRepository.save(new MessageEntity(receivedMessage.username(), receivedMessage.content()));
        sessionManager.getSessions().stream()
                .filter(not(session -> session.getId().equals(senderSession.getId())))
                .forEach(participantSession -> sessionManager.sendMessage(participantSession, receivedMessage));
    }

    @Override
    public Class<WriteMessageRequest> getRequestType() {
        return WriteMessageRequest.class;
    }
}
