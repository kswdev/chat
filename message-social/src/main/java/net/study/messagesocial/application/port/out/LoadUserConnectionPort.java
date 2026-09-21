package net.study.messagesocial.application.port.out;

import net.study.messagesocial.domain.userconnection.UserConnection;

import java.util.Optional;

public interface LoadUserConnectionPort {
    Optional<UserConnection> getUserConnection(Long inviterId, Long inviteeId);
}
