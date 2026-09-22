package net.study.messagesocial.domain.userconnectioncount;

public class LimitExceededException extends RuntimeException {

    private final Long userId;

    public LimitExceededException(Long userId) {
        super("Connection limit exceeded for user: " + userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}