package net.study.messagesocial.application.port.out;

import java.util.Optional;

public interface LoadUserPort {
    Optional<Long> getUserIdByInviteCode(String inviteCode);
    Optional<Long> getUserIdByUsername(String username);
    Optional<String> getUsername(Long userId);
    Optional<String> getInviteCode(Long userId);
}
