package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.QuitRequestRecord;
import net.study.messagesystem.dto.kafka.QuitResponseRecord;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuitRequestRecordHandler implements BaseRecordHandler<QuitRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(QuitRequestRecord record) {
        UserId userId = record.userId();

        ResultType result;
        try {
            result = channelService.quit(record.channelId(), userId);
        } catch (Exception e) {
            clientNotificationService.sendError(new ErrorResponseRecord(userId, ResultType.FAILED.getMessage(), MessageType.QUIT_REQUEST));
            return;
        }

        if (result.equals(ResultType.SUCCESS))
            clientNotificationService.sendMessage(userId, new QuitResponseRecord(userId, record.channelId()));
        else
            clientNotificationService.sendError(new ErrorResponseRecord(userId, result.getMessage(), MessageType.QUIT_REQUEST));
    }

    @Override
    public Class<QuitRequestRecord> getRequestType() {
        return QuitRequestRecord.class;
    }
}
