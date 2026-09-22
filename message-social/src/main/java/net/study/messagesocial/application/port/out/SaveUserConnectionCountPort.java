package net.study.messagesocial.application.port.out;

import net.study.messagesocial.domain.userconnectioncount.UserConnectionCount;

public interface SaveUserConnectionCountPort {
    void save(UserConnectionCount count);
}
