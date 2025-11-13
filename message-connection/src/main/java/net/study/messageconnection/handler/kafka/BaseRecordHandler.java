package net.study.messageconnection.handler.kafka;

import net.study.messageconnection.dto.kafka.RecordInterface;

public interface BaseRecordHandler <T extends RecordInterface> {

    void handleRecord(T record);
    Class<T> getRequestType();
}
