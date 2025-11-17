package net.study.messagesystem.handler.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.connection.Connection;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.kafka.FetchUserConnectionsRequestRecord;
import net.study.messagesystem.dto.kafka.FetchUserConnectionsResponseRecord;
import net.study.messagesystem.service.ClientNotificationService;
import net.study.messagesystem.service.UserConnectionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchConnectionsRequestRecordHandler implements BaseRecordHandler<FetchUserConnectionsRequestRecord> {

    private final UserConnectionService userConnectionService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void handleRecord(FetchUserConnectionsRequestRecord record) {
        UserId requestUserId = record.userId();
        UserConnectionStatus status = record.status();

        List<Connection> connections = userConnectionService.getUsersByStatus(requestUserId, status).stream()
                        .map(user -> new Connection(user.username(), status))
                        .toList();

        clientNotificationService.sendMessage(requestUserId, new FetchUserConnectionsResponseRecord(requestUserId, connections));
    }
    @Override
    public Class<FetchUserConnectionsRequestRecord> getRequestType() {
        return FetchUserConnectionsRequestRecord.class;
    }
}
