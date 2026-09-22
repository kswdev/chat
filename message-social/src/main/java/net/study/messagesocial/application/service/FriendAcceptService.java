package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.adapter.in.web.dto.error.FriendErrorCode;
import net.study.messagesocial.adapter.in.web.exception.ConnectionLimitExceededException;
import net.study.messagesocial.adapter.in.web.exception.ConnectionNotFoundException;
import net.study.messagesocial.adapter.in.web.exception.InvalidConnectionStatusException;
import net.study.messagesocial.adapter.in.web.exception.UserNotFoundException;
import net.study.messagesocial.application.port.in.FriendAccept;
import net.study.messagesocial.application.port.out.LoadUserConnectionCountPort;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.application.port.out.SaveUserConnectionCountPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import net.study.messagesocial.domain.userconnectioncount.LimitExceededException;
import net.study.messagesocial.domain.userconnectioncount.UserConnectionCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendAcceptService implements FriendAccept {

    private final LoadUserPort loadUser;
    private final SaveFriendPort saveFriend;
    private final LoadUserConnectionPort loadUserConnection;
    private final LoadUserConnectionCountPort loadUserConnectionCount;
    private final SaveUserConnectionCountPort saveUserConnectionCount;

    @Override
    public UserConnection accept(Long accepterId, String inviterUsername) {
        Long inviterId = loadUser
                .getUserIdByUsername(inviterUsername)
                .orElseThrow(() -> new UserNotFoundException(FriendErrorCode.USER_NOT_FOUND));

        UserConnection userConnection = loadUserConnection
                .getUserConnection(inviterId, accepterId)
                .orElseThrow(() -> new ConnectionNotFoundException(FriendErrorCode.CONNECTION_NOT_FOUND));

        if (userConnection.getStatus() != UserConnectionStatus.PENDING)
            throw new InvalidConnectionStatusException(FriendErrorCode.INVALID_CONNECTION_STATUS);

        applyConnectionCount(accepterId, inviterId);

        userConnection.changeStatus(UserConnectionStatus.ACCEPTED);
        saveFriend.save(userConnection);
        return userConnection;
    }

    private void applyConnectionCount(Long accepterId, Long inviterId) {
        Long first = Math.min(accepterId, inviterId);
        Long second = Math.max(accepterId, inviterId);

        UserConnectionCount firstCount = loadUserConnectionCount.lockAndLoad(first);
        UserConnectionCount secondCount = loadUserConnectionCount.lockAndLoad(second);

        try {
            firstCount.increase();
            secondCount.increase();
        } catch (LimitExceededException e) {
            throwConnectionLimitExceededLimit(accepterId, e);
        }

        saveUserConnectionCount.save(firstCount);
        saveUserConnectionCount.save(secondCount);
    }

    private static void throwConnectionLimitExceededLimit(Long accepterId, LimitExceededException e) {
        throw new ConnectionLimitExceededException(
                e.getUserId().equals(accepterId)
                        ? FriendErrorCode.CONNECTION_LIMIT_REACHED
                        : FriendErrorCode.CONNECTION_LIMIT_REACHED_BY_PARTNER);
    }
}
