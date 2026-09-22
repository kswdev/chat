package net.study.messageuser.dto.rest.user;

import net.study.messageuser.entity.user.UserEntity;

public record UserLookupResponse(Long userId, String username, String inviteCode) {

    public static UserLookupResponse from(UserEntity user) {
        return new UserLookupResponse(user.getUserId(), user.getUsername(), user.getInviteCode());
    }
}
