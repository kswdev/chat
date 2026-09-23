package net.study.messagesocial.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface LoadUserPort {
    Optional<Long> getUserIdByInviteCode(String inviteCode);
    Optional<Long> getUserIdByUsername(String username);
    Optional<String> getInviteCode(Long userId);
    Map<Long, String> getUsernames(Collection<Long> userIds);
}
