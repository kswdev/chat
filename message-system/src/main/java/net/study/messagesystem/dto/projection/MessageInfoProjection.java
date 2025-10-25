package net.study.messagesystem.dto.projection;

public interface MessageInfoProjection {
    Long getMessageSequence();
    Long getUserId();
    String getContent();
}
