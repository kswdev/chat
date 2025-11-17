package net.study.messagesystem.handler.kafka;

import net.study.messagesystem.dto.kafka.RecordInterface;

public interface BaseRecordHandler<T extends RecordInterface> {

    void handleRecord(T record);
    Class<T> getRequestType();
}
