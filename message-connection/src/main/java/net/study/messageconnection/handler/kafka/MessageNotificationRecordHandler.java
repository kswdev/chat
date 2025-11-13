package net.study.messageconnection.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messageconnection.dto.kafka.MessageNotificationRecord;
import net.study.messageconnection.service.MessageService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageNotificationRecordHandler implements BaseRecordHandler<MessageNotificationRecord> {

    private final MessageService messageService;

    public void handleRecord(MessageNotificationRecord record) {
        messageService.sendMessage(record);
    }

    @Override
    public Class<MessageNotificationRecord> getRequestType() {
        return MessageNotificationRecord.class;
    }
}
