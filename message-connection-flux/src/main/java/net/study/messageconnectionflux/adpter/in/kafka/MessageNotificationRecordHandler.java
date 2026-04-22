package net.study.messageconnectionflux.adpter.in.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnectionflux.application.dto.kafka.MessageNotificationRecord;
import net.study.messageconnectionflux.application.dto.websocket.outbound.MessageNotification;
import net.study.messageconnectionflux.application.port.out.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageNotificationRecordHandler implements BaseRecordHandler<MessageNotificationRecord> {

    private final ClientNotificationService clientNotificationService;

    public void handleRecord(MessageNotificationRecord record) {
        MessageNotification messageNotification = new MessageNotification(record.channelId(), record.messageSeqId(), record.username(), record.content());
        record.participantIds()
                .forEach(participantId ->
                        clientNotificationService.sendMessage(participantId, messageNotification, record));
    }

    @Override
    public Class<MessageNotificationRecord> getRequestType() {
        return MessageNotificationRecord.class;
    }
}
