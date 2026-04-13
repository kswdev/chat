package net.study.messageconnectionflux.handler.kafka;

import net.study.messageconnectionflux.dto.kafka.RecordInterface;

public interface BaseRecordHandler<T extends RecordInterface> {

    void handleRecord(T record);
    Class<T> getRequestType();
}
