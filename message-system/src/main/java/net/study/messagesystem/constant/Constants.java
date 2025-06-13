package net.study.messagesystem.constant;

import lombok.Getter;

public enum Constants {
    HTTP_SESSION_ID("HTTP_SESSION_ID");

    Constants(String value) {
        this.value = value;
    }

    @Getter private final String value;
}
