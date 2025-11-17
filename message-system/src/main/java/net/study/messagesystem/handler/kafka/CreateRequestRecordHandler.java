package net.study.messagesystem.handler.kafka;

import com.mysema.commons.lang.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.MessageType;
import net.study.messagesystem.constant.ResultType;
import net.study.messagesystem.domain.channel.Channel;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.CreateRequestRecord;
import net.study.messagesystem.dto.kafka.CreateResponseRecord;
import net.study.messagesystem.dto.kafka.ErrorResponseRecord;
import net.study.messagesystem.dto.kafka.JoinNotificationRecord;
import net.study.messagesystem.service.ChannelService;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRequestRecordHandler implements BaseRecordHandler<CreateRequestRecord> {

    private final UserService userService;
    private final ChannelService channelService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(CreateRequestRecord record) {
        UserId senderUserId = record.userId();
        List<UserId> participantIds = userService.getUserIds(record.participantUsernames());

        if (participantIds.isEmpty()) {
            clientNotificationService.sendMessage(
                    senderUserId,
                    new ErrorResponseRecord(senderUserId, ResultType.NOT_FOUND.getMessage(), MessageType.CREATE_REQUEST));

            return;
        }

        Pair<Optional<Channel>, ResultType> result;

        try {
            result = channelService.create(senderUserId, participantIds, record.title());
        } catch (Exception e) {
            clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, e.getMessage(), MessageType.CREATE_REQUEST));
            return;
        }

        result.getFirst()
              .ifPresentOrElse(channel -> {
                  clientNotificationService.sendMessage(senderUserId, new CreateResponseRecord(senderUserId, channel.channelId(), channel.title()));

                  participantIds.forEach((participantId) ->
                          CompletableFuture.runAsync(() ->
                                  clientNotificationService.sendMessage(participantId, new JoinNotificationRecord(participantId, channel.channelId(), channel.title()))
                          )
                  );

              }, () -> {
                  String errorMessage = result.getSecond().getMessage();
                  clientNotificationService.sendError(new ErrorResponseRecord(senderUserId, errorMessage, MessageType.CREATE_REQUEST));
              });

    }

    @Override
    public Class<CreateRequestRecord> getRequestType() {
        return CreateRequestRecord.class;
    }
}
