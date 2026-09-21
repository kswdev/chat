package net.study.messagesocial.application.port.out;

import net.study.messagesocial.domain.userconnection.UserConnection;

public interface SaveFriendPort {
    void save(UserConnection userConnection);
}
