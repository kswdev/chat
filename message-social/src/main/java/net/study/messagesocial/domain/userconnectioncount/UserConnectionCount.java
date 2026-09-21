package net.study.messagesocial.domain.userconnectioncount;

public class UserConnectionCount {
    private Long userId;
    private int count;

    private UserConnectionCount(Long userId) {
        this.userId = userId;
        this.count = 0;
    }

    public static UserConnectionCount create(Long userId) {
        return new UserConnectionCount(userId);
    }

    public void increase() { this.count++; }
    public void decrease() { this.count--; }
}
