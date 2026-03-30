package net.study.messagecommon.constant;

import lombok.Getter;

public enum IdKey {
    USER_ID("USER_ID"),
    CHANNEL_ID("channel_id");

    IdKey(String value) {
        this.value = value;
    }

    @Getter private final String value;
}
