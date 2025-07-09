package net.study.messagesystem.dto.websocket.inbound;

import lombok.Getter;

public abstract class BaseRequest {
    @Getter private final String type;

    public BaseRequest(String type) {
        this.type = type;
    }
}
