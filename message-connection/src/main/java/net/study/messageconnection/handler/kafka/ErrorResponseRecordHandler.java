package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.ErrorResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.ErrorResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorResponseRecordHandler implements BaseRecordHandler<ErrorResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(ErrorResponseRecord errorResponseRecord) {
        clientNotificationService.sendMessage(
                errorResponseRecord.userId(),
                new ErrorResponse(errorResponseRecord.messageType(), errorResponseRecord.message()),
                errorResponseRecord);
    }

    @Override
    public Class<ErrorResponseRecord> getRequestType() {
        return ErrorResponseRecord.class;
    }
}
