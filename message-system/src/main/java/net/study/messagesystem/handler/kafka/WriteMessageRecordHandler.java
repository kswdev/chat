package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.dto.kafka.WriteMessageRecord;
import net.study.messagesystem.service.MessageService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteMessageRecordHandler implements BaseRecordHandler<WriteMessageRecord> {

    private final MessageService messageService;

    @Override
    public void handleRecord(WriteMessageRecord record) {
        messageService.sendMessage(record);
    }

    @Override
    public Class<WriteMessageRecord> getRequestType() {
        return WriteMessageRecord.class;
    }
}
