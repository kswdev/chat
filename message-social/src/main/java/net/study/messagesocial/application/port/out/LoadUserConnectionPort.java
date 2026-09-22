package net.study.messagesocial.application.port.out;

import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;

import java.util.List;
import java.util.Optional;

public interface LoadUserConnectionPort {
    Optional<UserConnection> getUserConnection(Long inviterId, Long inviteeId);
    List<UserConnection> findByUserIdAndStatus(Long userId, UserConnectionStatus status);
}
