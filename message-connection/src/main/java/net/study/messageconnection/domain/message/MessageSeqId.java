package net.study.messageconnection.domain.message;

import com.fasterxml.jackson.annotation.JsonValue;

public record MessageSeqId(@JsonValue Long id) {

    public MessageSeqId {
        if (id == null || id < 0)
            throw new IllegalArgumentException("Invalid MessageSeqId");
    }
}
