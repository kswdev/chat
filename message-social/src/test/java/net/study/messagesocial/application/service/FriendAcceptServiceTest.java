package net.study.messagesocial.application.service;

import net.study.messagesocial.adapter.in.web.exception.ConnectionLimitExceededException;
import net.study.messagesocial.adapter.in.web.exception.ConnectionNotFoundException;
import net.study.messagesocial.adapter.in.web.exception.InvalidConnectionStatusException;
import net.study.messagesocial.adapter.in.web.exception.UserNotFoundException;
import net.study.messagesocial.application.port.out.LoadUserConnectionCountPort;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.application.port.out.SaveUserConnectionCountPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import net.study.messagesocial.domain.userconnectioncount.UserConnectionCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FriendAcceptServiceTest {

    @InjectMocks
    private FriendAcceptService friendAccept;

    @Mock
    private LoadUserPort loadUser;

    @Mock
    private SaveFriendPort saveFriend;

    @Mock
    private LoadUserConnectionPort loadUserConnection;

    @Mock
    private LoadUserConnectionCountPort loadUserConnectionCount;

    @Mock
    private SaveUserConnectionCountPort saveUserConnectionCount;

    // INVITER < ACCEPTER로 고정해서 "오름차순 잠금 순서" 검증이 항상 같은 순서를 기대하게 한다.
    private static final Long INVITER = 1L;
    private static final Long ACCEPTER = 2L;
    private static final String INVITER_USERNAME = "inviter";

    @Test
    void accept_throws_when_inviter_username_not_found() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> friendAccept.accept(ACCEPTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        then(saveFriend).should(never()).save(any());
    }

    @Test
    void accept_throws_when_no_pending_connection_exists() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, ACCEPTER)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> friendAccept.accept(ACCEPTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(ConnectionNotFoundException.class);
        then(saveFriend).should(never()).save(any());
        then(loadUserConnectionCount).should(never()).lockAndLoad(any());
    }

    @Test
    void accept_throws_when_connection_is_not_pending() {
        UserConnection connection = pendingConnection();
        connection.changeStatus(UserConnectionStatus.ACCEPTED);

        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, ACCEPTER)).willReturn(Optional.of(connection));

        Throwable thrown = catchThrowable(() -> friendAccept.accept(ACCEPTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(InvalidConnectionStatusException.class);
        then(saveFriend).should(never()).save(any());
        then(loadUserConnectionCount).should(never()).lockAndLoad(any());
    }

    @Test
    void accept_locks_both_users_in_ascending_id_order() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, ACCEPTER)).willReturn(Optional.of(pendingConnection()));
        given(loadUserConnectionCount.lockAndLoad(INVITER)).willReturn(UserConnectionCount.of(INVITER, 0));
        given(loadUserConnectionCount.lockAndLoad(ACCEPTER)).willReturn(UserConnectionCount.of(ACCEPTER, 0));

        friendAccept.accept(ACCEPTER, INVITER_USERNAME);

        InOrder order = inOrder(loadUserConnectionCount);
        order.verify(loadUserConnectionCount).lockAndLoad(INVITER);
        order.verify(loadUserConnectionCount).lockAndLoad(ACCEPTER);
    }

    @Test
    void accept_throws_when_accepter_reached_connection_limit() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, ACCEPTER)).willReturn(Optional.of(pendingConnection()));
        given(loadUserConnectionCount.lockAndLoad(INVITER)).willReturn(UserConnectionCount.of(INVITER, 0));
        given(loadUserConnectionCount.lockAndLoad(ACCEPTER))
                .willReturn(UserConnectionCount.of(ACCEPTER, UserConnectionCount.LIMIT));

        Throwable thrown = catchThrowable(() -> friendAccept.accept(ACCEPTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(ConnectionLimitExceededException.class);
        then(saveFriend).should(never()).save(any());
        then(saveUserConnectionCount).should(never()).save(any());
    }

    @Test
    void accept_throws_when_inviter_reached_connection_limit() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, ACCEPTER)).willReturn(Optional.of(pendingConnection()));
        given(loadUserConnectionCount.lockAndLoad(INVITER))
                .willReturn(UserConnectionCount.of(INVITER, UserConnectionCount.LIMIT));
        given(loadUserConnectionCount.lockAndLoad(ACCEPTER)).willReturn(UserConnectionCount.of(ACCEPTER, 0));

        Throwable thrown = catchThrowable(() -> friendAccept.accept(ACCEPTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(ConnectionLimitExceededException.class);
        then(saveFriend).should(never()).save(any());
        then(saveUserConnectionCount).should(never()).save(any());
    }

    @Test
    void accept_increases_and_saves_both_counts_when_below_limit() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, ACCEPTER)).willReturn(Optional.of(pendingConnection()));
        given(loadUserConnectionCount.lockAndLoad(INVITER)).willReturn(UserConnectionCount.of(INVITER, 3));
        given(loadUserConnectionCount.lockAndLoad(ACCEPTER)).willReturn(UserConnectionCount.of(ACCEPTER, 5));

        UserConnection result = friendAccept.accept(ACCEPTER, INVITER_USERNAME);

        assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.ACCEPTED);
        then(saveFriend).should().save(result);

        ArgumentCaptor<UserConnectionCount> captor = ArgumentCaptor.forClass(UserConnectionCount.class);
        then(saveUserConnectionCount).should(times(2)).save(captor.capture());
        List<UserConnectionCount> saved = captor.getAllValues();

        assertThat(saved).extracting(UserConnectionCount::getUserId).containsExactlyInAnyOrder(INVITER, ACCEPTER);
        saved.forEach(count -> {
            int expected = count.getUserId().equals(INVITER) ? 4 : 6;
            assertThat(count.getCount()).isEqualTo(expected);
        });
    }

    private static UserConnection pendingConnection() {
        return UserConnection.builder()
                .inviterId(INVITER)
                .inviteeId(ACCEPTER)
                .status(UserConnectionStatus.PENDING)
                .build();
    }
}
