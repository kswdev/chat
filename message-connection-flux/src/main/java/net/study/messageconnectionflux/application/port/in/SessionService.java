package net.study.messageconnectionflux.application.port.in;

import net.study.messageconnectionflux.domain.user.UserId;
import reactor.core.publisher.Mono;

public interface SessionService {

    void setOnline(UserId userId, boolean status);
    Mono<Long> deActiveChannel(UserId userId);
    void refreshTTL(UserId userId);
}
