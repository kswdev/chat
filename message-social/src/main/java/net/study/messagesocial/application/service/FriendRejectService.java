package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.adapter.in.web.dto.error.FriendErrorCode;
import net.study.messagesocial.adapter.in.web.exception.ConnectionNotFoundException;
import net.study.messagesocial.adapter.in.web.exception.InvalidConnectionStatusException;
import net.study.messagesocial.adapter.in.web.exception.UserNotFoundException;
import net.study.messagesocial.application.port.in.FriendReject;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendRejectService implements FriendReject {

    private final LoadUserPort loadUser;
    private final SaveFriendPort saveFriend;
    private final LoadUserConnectionPort loadUserConnection;

    @Override
    public UserConnection reject(Long rejecterId, String inviterUsername) {
        Long inviterId = loadUser
                .getUserIdByUsername(inviterUsername)
                .orElseThrow(() -> new UserNotFoundException(FriendErrorCode.USER_NOT_FOUND));

        UserConnection userConnection = loadUserConnection
                .getUserConnection(inviterId, rejecterId)
                .orElseThrow(() -> new ConnectionNotFoundException(FriendErrorCode.CONNECTION_NOT_FOUND));

        if (userConnection.getStatus() != UserConnectionStatus.PENDING)
            throw new InvalidConnectionStatusException(FriendErrorCode.INVALID_CONNECTION_STATUS);

        userConnection.changeStatus(UserConnectionStatus.REJECTED);
        saveFriend.save(userConnection);
        return userConnection;
    }
}
