package net.study.messagesystem.dto.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record InviteCode(
        @JsonValue String code
) {

    @JsonCreator
    public InviteCode {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Invalid UserId");
    }
}
