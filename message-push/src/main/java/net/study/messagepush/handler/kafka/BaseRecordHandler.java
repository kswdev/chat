package net.study.messagepush.handler.kafka;

import net.study.messagepush.dto.kafka.inbound.RecordInterface;

public interface BaseRecordHandler<T extends RecordInterface> {
    void handleRequest(T request);
    Class<T> getRequestType();
}
