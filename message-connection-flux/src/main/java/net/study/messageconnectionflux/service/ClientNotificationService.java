package net.study.messageconnectionflux.service;

import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.dto.websocket.outbound.ErrorResponse;

public interface ClientNotificationService {

    void sendError(UserId userId, ErrorResponse errorResponse);
}
