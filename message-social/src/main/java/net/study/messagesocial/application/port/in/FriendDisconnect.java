package net.study.messagesocial.application.port.in;

import net.study.messagesocial.domain.userconnection.UserConnection;
import org.springframework.stereotype.Service;

@Service
public interface FriendDisconnect {
    UserConnection disconnect(Long senderId, String partnerUsername);
}
