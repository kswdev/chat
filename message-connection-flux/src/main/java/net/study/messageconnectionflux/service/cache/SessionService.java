package net.study.messageconnectionflux.service.cache;

import net.study.messageconnectionflux.domain.user.UserId;

public interface SessionService {

    void setOnline(UserId userId, boolean status);
    boolean deActiveChannel(UserId userId);
    void refreshTTL(UserId userId);
}
