package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.QuitResponseRecord;
import net.study.messageconnection.dto.websocket.outbound.QuitResponse;
import net.study.messageconnection.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuitResponseRecordHandler implements BaseRecordHandler<QuitResponseRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(QuitResponseRecord record) {
        clientNotificationService.sendMessage(
                record.userId(),
                new QuitResponse(record.channelId()),
                record);
    }

    @Override
    public Class<QuitResponseRecord> getRequestType() {
        return QuitResponseRecord.class;
    }
}
