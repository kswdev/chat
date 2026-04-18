package net.study.messageconnectionflux.adpter.in.kafka;

import net.study.messageconnectionflux.application.dto.kafka.RecordInterface;

public interface BaseRecordHandler<T extends RecordInterface> {

    void handleRecord(T record);
    Class<T> getRequestType();
}
