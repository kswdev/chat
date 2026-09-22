package net.study.messagesocial.application.service;

import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FriendConnectionQueryServiceTest {

    @InjectMocks
    private FriendConnectionQueryService friendConnectionQuery;

    @Mock
    private LoadUserConnectionPort loadUserConnection;

    @Test
    void getConnections_delegates_to_port_with_given_userId_and_status() {
        Long userId = 1L;
        UserConnection connection = UserConnection.builder()
                .inviterId(userId).inviteeId(2L).status(UserConnectionStatus.ACCEPTED).build();

        given(loadUserConnection.findByUserIdAndStatus(userId, UserConnectionStatus.ACCEPTED))
                .willReturn(List.of(connection));

        List<UserConnection> result = friendConnectionQuery.getConnections(userId, UserConnectionStatus.ACCEPTED);

        assertThat(result).containsExactly(connection);
    }

    @Test
    void getConnections_returns_empty_list_when_none_found() {
        given(loadUserConnection.findByUserIdAndStatus(1L, UserConnectionStatus.PENDING))
                .willReturn(List.of());

        List<UserConnection> result = friendConnectionQuery.getConnections(1L, UserConnectionStatus.PENDING);

        assertThat(result).isEmpty();
    }
}
