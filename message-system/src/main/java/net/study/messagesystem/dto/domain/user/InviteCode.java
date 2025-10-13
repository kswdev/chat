package net.study.messagesystem.dto.domain.user;

import com.fasterxml.jackson.annotation.JsonValue;

public record InviteCode(
        @JsonValue String code
) {

    public InviteCode {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Invalid UserId");
    }
}
