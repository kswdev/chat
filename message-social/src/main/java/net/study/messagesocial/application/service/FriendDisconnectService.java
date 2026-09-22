package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.adapter.in.web.dto.error.FriendErrorCode;
import net.study.messagesocial.adapter.in.web.exception.ConnectionNotFoundException;
import net.study.messagesocial.adapter.in.web.exception.InvalidConnectionStatusException;
import net.study.messagesocial.adapter.in.web.exception.UserNotFoundException;
import net.study.messagesocial.application.port.in.FriendDisconnect;
import net.study.messagesocial.application.port.out.LoadUserConnectionCountPort;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.application.port.out.SaveUserConnectionCountPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import net.study.messagesocial.domain.userconnectioncount.UserConnectionCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendDisconnectService implements FriendDisconnect {

    private final LoadUserPort loadUser;
    private final SaveFriendPort saveFriend;
    private final LoadUserConnectionPort loadUserConnection;
    private final LoadUserConnectionCountPort loadUserConnectionCount;
    private final SaveUserConnectionCountPort saveUserConnectionCount;

    @Override
    public UserConnection disconnect(Long senderId, String partnerUsername) {
        Long partnerId = loadUser
                .getUserIdByUsername(partnerUsername)
                .orElseThrow(() -> new UserNotFoundException(FriendErrorCode.USER_NOT_FOUND));

        UserConnection userConnection = loadUserConnection
                .getUserConnection(senderId, partnerId)
                .or(() -> loadUserConnection.getUserConnection(partnerId, senderId))
                .orElseThrow(() -> new ConnectionNotFoundException(FriendErrorCode.CONNECTION_NOT_FOUND));

        if (userConnection.getStatus() != UserConnectionStatus.ACCEPTED)
            throw new InvalidConnectionStatusException(FriendErrorCode.INVALID_CONNECTION_STATUS);

        applyConnectionCount(senderId, partnerId);

        userConnection.changeStatus(UserConnectionStatus.DISCONNECTED);
        saveFriend.save(userConnection);
        return userConnection;
    }

    private void applyConnectionCount(Long senderId, Long partnerId) {
        Long first = Math.min(senderId, partnerId);
        Long second = Math.max(senderId, partnerId);

        UserConnectionCount firstCount = lockAndLoad(first);
        UserConnectionCount secondCount = lockAndLoad(second);

        firstCount.decrease();
        secondCount.decrease();
        saveUserConnectionCount.save(firstCount);
        saveUserConnectionCount.save(secondCount);
    }

    private UserConnectionCount lockAndLoad(Long userId) {
        return loadUserConnectionCount.lockAndLoad(userId);
    }
}
