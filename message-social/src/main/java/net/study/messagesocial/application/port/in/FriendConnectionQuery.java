package net.study.messagesocial.application.port.in;

import net.study.messagesocial.domain.userconnection.FriendConnectionSummary;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FriendConnectionQuery {
    List<FriendConnectionSummary> getConnections(Long userId, UserConnectionStatus status);
    long countAccepted(Long userId, List<Long> partnerIds);
}
