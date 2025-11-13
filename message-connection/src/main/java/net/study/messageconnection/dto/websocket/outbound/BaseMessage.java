package net.study.messageconnection.dto.websocket.outbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class BaseMessage {

    private final String type;
}
