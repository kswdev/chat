package net.study.messagesystem.constant;

import lombok.Getter;

public enum IdKey {
    HTTP_SESSION_ID("HTTP_SESSION_ID"),
    USER_ID("USER_ID"),
    CHANNEL_ID("channel_id");
    
    IdKey(String value) {
        this.value = value;
    }

    @Getter private final String value;
}
