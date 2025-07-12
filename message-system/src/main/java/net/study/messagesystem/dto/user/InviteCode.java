package net.study.messagesystem.dto.user;

public record InviteCode(String code) {

    public InviteCode {
        if (code == null || code.isEmpty())
            throw new IllegalArgumentException("Invalid UserId");
    }
}
