package net.study.messagesystem.handler.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.projection.UserIdProjection;
import net.study.messagesystem.dto.websocket.inbound.WriteMessageRequest;
import net.study.messagesystem.dto.websocket.outbound.MessageNotification;
import net.study.messagesystem.entity.messae.MessageEntity;
import net.study.messagesystem.repository.MessageRepository;
import net.study.messagesystem.repository.UserRepository;
import net.study.messagesystem.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

import static java.util.function.Predicate.not;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageRequestHandler implements BaseRequestHandler<WriteMessageRequest> {

    private final WebSocketSessionManager sessionManager;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public void handleRequest(WebSocketSession senderSession, WriteMessageRequest request) {
        MessageNotification receivedMessage = new MessageNotification(request.getUsername(), request.getContent());
        Optional<Long> userId = userRepository.findUserIdByUsername(request.getUsername())
                .map(UserIdProjection::getUserId);

        if (userId.isEmpty()) {
            log.error("Write message failed. User not exists. username: {}", request.getUsername());
            return;
        }

        messageRepository.save(new MessageEntity(userId.get(), receivedMessage.getContent()));
        sessionManager.getSessions().stream()
                .filter(not(session -> session.getId().equals(senderSession.getId())))
                .forEach(participantSession -> sessionManager.sendMessage(participantSession, receivedMessage));
    }

    @Override
    public Class<WriteMessageRequest> getRequestType() {
        return WriteMessageRequest.class;
    }
}
