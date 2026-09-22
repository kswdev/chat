package net.study.messagesocial.application.port.in;

import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FriendConnectionQuery {
    List<UserConnection> getConnections(Long userId, UserConnectionStatus status);
}
