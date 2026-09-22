package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.adapter.in.web.dto.error.FriendErrorCode;
import net.study.messagesocial.adapter.in.web.exception.UserNotFoundException;
import net.study.messagesocial.application.port.in.FriendInviteCodeQuery;
import net.study.messagesocial.application.port.out.LoadUserPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendInviteCodeQueryService implements FriendInviteCodeQuery {

    private final LoadUserPort loadUser;

    @Override
    @Transactional(readOnly = true)
    public String getInviteCode(Long userId) {
        return loadUser
                .getInviteCode(userId)
                .orElseThrow(() -> new UserNotFoundException(FriendErrorCode.USER_NOT_FOUND));
    }
}
