package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.adapter.in.web.dto.error.FriendErrorCode;
import net.study.messagesocial.adapter.in.web.exception.AlreadyConnectedException;
import net.study.messagesocial.adapter.in.web.exception.AlreadyInvitedException;
import net.study.messagesocial.adapter.in.web.exception.InvalidInviteCodeException;
import net.study.messagesocial.application.port.in.FriendInvite;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendInviteService implements FriendInvite {

    private final LoadUserPort loadUser;
    private final SaveFriendPort saveFriend;
    private final LoadUserConnectionPort loadUserConnection;

    @Override
    public UserConnection invite(Long inviterId, String inviteCode) {

        Long inviteeId = loadUser
                .getUserIdByInviteCode(inviteCode)
                .orElseThrow(() -> new InvalidInviteCodeException(FriendErrorCode.INVITE_CODE_NOT_FOUND));

        return loadUserConnection
                .getUserConnection(inviterId, inviteeId)
                .map(this::process)
                .orElseGet(() -> createUserConnectionAndSave(inviterId, inviteeId));
    }

    private UserConnection process(UserConnection userConnection) {
        if (userConnection == null)
            return null;

        return switch (userConnection.getStatus()) {
            case NONE, DISCONNECTED -> inviteRequest(userConnection);
            case PENDING, REJECTED -> throw new AlreadyInvitedException(FriendErrorCode.ALREADY_INVITED);
            case ACCEPTED -> throw new AlreadyConnectedException(FriendErrorCode.ALREADY_FRIEND);
        };
    }

    private UserConnection inviteRequest(UserConnection userConnection) {
        userConnection.changeStatus(UserConnectionStatus.PENDING);
        saveFriend.save(userConnection);
        return userConnection;
    }

    private UserConnection createUserConnectionAndSave(Long inviterId, Long inviteeId) {
        UserConnection userConnection = createUserConnection(inviterId, inviteeId);
        saveFriend.save(userConnection);
        return userConnection;
    }

    private static UserConnection createUserConnection(Long inviterId, Long inviteeId) {
        return UserConnection.builder()
                .inviterId(inviterId)
                .inviteeId(inviteeId)
                .status(UserConnectionStatus.PENDING)
                .build();
    }
}
