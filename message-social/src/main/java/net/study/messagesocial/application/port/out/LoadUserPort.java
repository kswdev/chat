package net.study.messagesocial.application.port.out;

import java.util.Optional;

public interface LoadUserPort {
    Optional<Long> getUserIdByInviteCode(String inviteCode);
}
