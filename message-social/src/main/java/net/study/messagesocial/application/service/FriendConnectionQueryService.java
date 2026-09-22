package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.application.port.in.FriendConnectionQuery;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendConnectionQueryService implements FriendConnectionQuery {

    private final LoadUserConnectionPort loadUserConnection;

    @Override
    @Transactional(readOnly = true)
    public List<UserConnection> getConnections(Long userId, UserConnectionStatus status) {
        return loadUserConnection.findByUserIdAndStatus(userId, status);
    }
}
