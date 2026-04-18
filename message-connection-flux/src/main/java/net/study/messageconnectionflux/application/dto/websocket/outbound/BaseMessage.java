package net.study.messageconnectionflux.application.dto.websocket.outbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class BaseMessage {

    private final String type;
}
