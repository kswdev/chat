package net.study.messageconnectionflux.application.port.out;

import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;
import net.study.messageconnectionflux.application.dto.websocket.outbound.BaseMessage;
import net.study.messageconnectionflux.domain.user.UserId;
import net.study.messageconnectionflux.application.dto.websocket.outbound.ErrorResponse;

public interface ClientNotificationService {

    void sendError(UserId userId, ErrorResponse errorResponse);
    void sendMessage(UserId userId, BaseMessage message, RecordInterface recordInterface);
}
