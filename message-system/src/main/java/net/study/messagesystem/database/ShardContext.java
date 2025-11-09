package net.study.messagesystem.database;

public abstract class ShardContext {

    private ShardContext() {}

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static Long getChannelId() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }

    public static void setChannelId(Long channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("channelId can not be null");
        }
        threadLocal.set(channelId);
    }

    public static final class ShardContextScope implements AutoCloseable {

        public ShardContextScope(Long channelId) {
            ShardContext.setChannelId(channelId);
        }

        @Override
        public void close() {
            ShardContext.clear();
        }
    }
}
