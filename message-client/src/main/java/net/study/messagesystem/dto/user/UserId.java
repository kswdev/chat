package net.study.messagesystem.dto.user;

public record UserId(Long id) {

    public UserId {
        if (id == null || id < 0)
            throw new IllegalArgumentException("Invalid UserId");
    }
}
