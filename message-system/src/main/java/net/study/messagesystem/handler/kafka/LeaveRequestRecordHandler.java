package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.LeaveRequestRecord;
import net.study.messagesystem.dto.kafka.LeaveResponseRecord;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveRequestRecordHandler implements BaseRecordHandler<LeaveRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(LeaveRequestRecord record) {
        UserId userId = record.userId();

        if(channelService.leave(userId))
            clientNotificationService.sendMessage(userId, new LeaveResponseRecord(userId));
        else
            clientNotificationService.sendError(new ErrorResponseRecord(userId, ResultType.FAILED.getMessage(), MessageType.LEAVE_REQUEST));
    }

    @Override
    public Class<LeaveRequestRecord> getRequestType() {
        return LeaveRequestRecord.class;
    }
}
