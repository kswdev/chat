package net.study.messagecommon.constant;

import lombok.Getter;

public enum IdKey {
    USER_ID("USER_ID"),
    USERNAME("USERNAME"),
    CHANNEL_ID("channel_id"),
    INTERNAL_API_KEY("X-Internal-Api-Key");

    IdKey(String value) {
        this.value = value;
    }

    @Getter private final String value;
}
