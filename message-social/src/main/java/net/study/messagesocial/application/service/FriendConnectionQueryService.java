package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.application.port.in.FriendConnectionQuery;
import net.study.messagesocial.domain.userconnection.FriendConnectionSummary;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FriendConnectionQueryService implements FriendConnectionQuery {

    private final LoadUserConnectionPort loadUserConnection;
    private final LoadUserPort loadUser;

    @Override
    @Transactional(readOnly = true)
    public List<FriendConnectionSummary> getConnections(Long userId, UserConnectionStatus status) {
        List<UserConnection> connections = loadUserConnection.findByUserIdAndStatus(userId, status);

        List<Long> partnerIds = connections.stream().map(connection -> partnerOf(connection, userId)).toList();
        Map<Long, String> usernames = loadUser.getUsernames(partnerIds);

        return connections.stream()
                .map(connection -> {
                    Long partnerId = partnerOf(connection, userId);
                    return new FriendConnectionSummary(partnerId, usernames.get(partnerId), connection.getStatus());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAccepted(Long userId, List<Long> partnerIds) {
        if (partnerIds.isEmpty())
            return 0;

        return loadUserConnection.countByUserIdAndPartnerIdsAndStatus(userId, partnerIds, UserConnectionStatus.ACCEPTED);
    }

    private Long partnerOf(UserConnection connection, Long userId) {
        return connection.getInviterId().equals(userId) ? connection.getInviteeId() : connection.getInviterId();
    }
}
