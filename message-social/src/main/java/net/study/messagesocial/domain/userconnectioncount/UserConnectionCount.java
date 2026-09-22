package net.study.messagesocial.domain.userconnectioncount;

public class UserConnectionCount {
    public static final int LIMIT = 10;

    private final Long userId;
    private int count;

    private UserConnectionCount(Long userId, int count) {
        this.userId = userId;
        this.count = count;
    }

    public static UserConnectionCount create(Long userId) {
        return new UserConnectionCount(userId, 0);
    }

    public static UserConnectionCount of(Long userId, int count) {
        return new UserConnectionCount(userId, count);
    }

    public void increase() {
        if (isAtLimit())
            throw new LimitExceededException(userId);
        this.count++;
    }

    public void decrease() {
        this.count = Math.max(0, this.count - 1);
    }

    public Long getUserId() { return userId; }
    public int getCount() { return count; }

    private boolean isAtLimit() {
        return count >= LIMIT;
    }
}