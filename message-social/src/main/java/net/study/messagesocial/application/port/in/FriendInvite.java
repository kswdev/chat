package net.study.messagesocial.application.port.in;

import org.springframework.stereotype.Service;

@Service
public interface FriendInvite {
    void invite(Long userId, String inviteCode);
}
