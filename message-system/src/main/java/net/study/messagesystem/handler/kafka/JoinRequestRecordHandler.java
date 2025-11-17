package net.study.messagesystem.handler.kafka;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.Channel;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.JoinRequestRecord;
import net.study.messagesystem.dto.kafka.JoinResponseRecord;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinRequestRecordHandler implements BaseRecordHandler<JoinRequestRecord> {

    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(JoinRequestRecord record) {
        UserId senderUserId = record.userId();
        Pair<Optional<Channel>, ResultType> result;

        try {
            result = channelService.join(record.inviteCode(), senderUserId);
        } catch (Exception e) {
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, e.getMessage(), ResultType.FAILED.getMessage()));
            return;
        }

        result.getFirst()
              .ifPresentOrElse(channel ->
                      clientNotificationService.sendMessage(senderUserId, new JoinResponseRecord(senderUserId, channel.channelId(), channel.title())),
                      () -> clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, result.getSecond().getMessage(), MessageType.JOIN_REQUEST)));
    }

    @Override
    public Class<JoinRequestRecord> getRequestType() {
        return JoinRequestRecord.class;
    }
}
