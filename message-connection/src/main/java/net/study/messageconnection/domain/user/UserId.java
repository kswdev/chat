package net.study.messageconnection.domain.user;

import com.fasterxml.jackson.annotation.JsonValue;

public record UserId(@JsonValue Long id) {

    public UserId {
        if (id == null || id < 0)
            throw new IllegalArgumentException("Invalid UserId");
    }
}
