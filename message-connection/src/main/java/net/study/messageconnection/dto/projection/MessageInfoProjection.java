package net.study.messageconnection.dto.projection;

public interface MessageInfoProjection {
    Long getMessageSequence();
    Long getUserId();
    String getContent();
}
