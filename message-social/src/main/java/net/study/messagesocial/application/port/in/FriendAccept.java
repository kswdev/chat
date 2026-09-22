package net.study.messagesocial.application.port.in;

import net.study.messagesocial.domain.userconnection.UserConnection;
import org.springframework.stereotype.Service;

@Service
public interface FriendAccept {
    UserConnection accept(Long accepterId, String inviterUsername);
}
