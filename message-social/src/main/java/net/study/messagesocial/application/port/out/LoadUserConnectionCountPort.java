package net.study.messagesocial.application.port.out;

import net.study.messagesocial.domain.userconnectioncount.UserConnectionCount;

import java.util.Optional;

public interface LoadUserConnectionCountPort {
    Optional<UserConnectionCount> loadUserConnectionCount(Long userId);
}
