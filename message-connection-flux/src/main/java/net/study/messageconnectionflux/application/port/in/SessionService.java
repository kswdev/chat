package net.study.messageconnectionflux.application.port.in;

import net.study.messageconnectionflux.domain.user.UserId;
import reactor.core.publisher.Mono;

public interface SessionService {

    Mono<Boolean> setOnline(UserId userId, boolean status);
    Mono<Long> deActiveChannel(UserId userId);
    Mono<Boolean> refreshTTL(UserId userId);
}
