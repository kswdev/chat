package net.study.messageconnectionflux.dto.websocket.outbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class BaseMessage {

    private final String type;
}
