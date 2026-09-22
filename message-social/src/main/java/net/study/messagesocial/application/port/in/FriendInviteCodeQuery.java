package net.study.messagesocial.application.port.in;

import org.springframework.stereotype.Service;

@Service
public interface FriendInviteCodeQuery {
    String getInviteCode(Long userId);
}
