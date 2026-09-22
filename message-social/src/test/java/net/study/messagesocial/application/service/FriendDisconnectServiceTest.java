package net.study.messagesocial.application.service;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FriendDisconnectServiceTest {

    @InjectMocks
    private FriendDisconnectService friendDisconnect;

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

    private static final Long SENDER = 1L;
    private static final Long PARTNER = 2L;
    private static final String PARTNER_USERNAME = "partner";

    @Test
    void disconnect_throws_when_partner_username_not_found() {
        given(loadUser.getUserIdByUsername(PARTNER_USERNAME)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> friendDisconnect.disconnect(SENDER, PARTNER_USERNAME));

        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        then(saveFriend).should(never()).save(any());
    }

    @Test
    void disconnect_throws_when_no_connection_exists_in_either_direction() {
        given(loadUser.getUserIdByUsername(PARTNER_USERNAME)).willReturn(Optional.of(PARTNER));
        given(loadUserConnection.getUserConnection(SENDER, PARTNER)).willReturn(Optional.empty());
        given(loadUserConnection.getUserConnection(PARTNER, SENDER)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> friendDisconnect.disconnect(SENDER, PARTNER_USERNAME));

        assertThat(thrown).isInstanceOf(ConnectionNotFoundException.class);
        then(saveFriend).should(never()).save(any());
        then(loadUserConnectionCount).should(never()).lockAndLoad(any());
    }

    @Test
    void disconnect_finds_connection_when_sender_was_the_original_invitee() {
        UserConnection connection = UserConnection.builder()
                .inviterId(PARTNER).inviteeId(SENDER).status(UserConnectionStatus.ACCEPTED).build();

        given(loadUser.getUserIdByUsername(PARTNER_USERNAME)).willReturn(Optional.of(PARTNER));
        given(loadUserConnection.getUserConnection(SENDER, PARTNER)).willReturn(Optional.empty());
        given(loadUserConnection.getUserConnection(PARTNER, SENDER)).willReturn(Optional.of(connection));
        given(loadUserConnectionCount.lockAndLoad(SENDER)).willReturn(UserConnectionCount.of(SENDER, 1));
        given(loadUserConnectionCount.lockAndLoad(PARTNER)).willReturn(UserConnectionCount.of(PARTNER, 1));

        UserConnection result = friendDisconnect.disconnect(SENDER, PARTNER_USERNAME);

        assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.DISCONNECTED);
        then(saveFriend).should().save(result);
    }

    @Test
    void disconnect_throws_when_connection_is_not_accepted() {
        UserConnection connection = UserConnection.builder()
                .inviterId(SENDER).inviteeId(PARTNER).status(UserConnectionStatus.PENDING).build();

        given(loadUser.getUserIdByUsername(PARTNER_USERNAME)).willReturn(Optional.of(PARTNER));
        given(loadUserConnection.getUserConnection(SENDER, PARTNER)).willReturn(Optional.of(connection));

        Throwable thrown = catchThrowable(() -> friendDisconnect.disconnect(SENDER, PARTNER_USERNAME));

        assertThat(thrown).isInstanceOf(InvalidConnectionStatusException.class);
        then(saveFriend).should(never()).save(any());
        then(loadUserConnectionCount).should(never()).lockAndLoad(any());
    }

    @Test
    void disconnect_decreases_and_saves_both_counts_when_accepted() {
        UserConnection connection = UserConnection.builder()
                .inviterId(SENDER).inviteeId(PARTNER).status(UserConnectionStatus.ACCEPTED).build();

        given(loadUser.getUserIdByUsername(PARTNER_USERNAME)).willReturn(Optional.of(PARTNER));
        given(loadUserConnection.getUserConnection(SENDER, PARTNER)).willReturn(Optional.of(connection));
        given(loadUserConnectionCount.lockAndLoad(SENDER)).willReturn(UserConnectionCount.of(SENDER, 4));
        given(loadUserConnectionCount.lockAndLoad(PARTNER)).willReturn(UserConnectionCount.of(PARTNER, 7));

        UserConnection result = friendDisconnect.disconnect(SENDER, PARTNER_USERNAME);

        assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.DISCONNECTED);
        then(saveFriend).should().save(result);

        ArgumentCaptor<UserConnectionCount> captor = ArgumentCaptor.forClass(UserConnectionCount.class);
        then(saveUserConnectionCount).should(times(2)).save(captor.capture());
        List<UserConnectionCount> saved = captor.getAllValues();

        assertThat(saved).extracting(UserConnectionCount::getUserId).containsExactlyInAnyOrder(SENDER, PARTNER);
        saved.forEach(count -> {
            int expected = count.getUserId().equals(SENDER) ? 3 : 6;
            assertThat(count.getCount()).isEqualTo(expected);
        });
    }

    @Test
    void disconnect_does_not_go_below_zero_when_count_already_zero() {
        UserConnection connection = UserConnection.builder()
                .inviterId(SENDER).inviteeId(PARTNER).status(UserConnectionStatus.ACCEPTED).build();

        given(loadUser.getUserIdByUsername(PARTNER_USERNAME)).willReturn(Optional.of(PARTNER));
        given(loadUserConnection.getUserConnection(SENDER, PARTNER)).willReturn(Optional.of(connection));
        given(loadUserConnectionCount.lockAndLoad(SENDER)).willReturn(UserConnectionCount.of(SENDER, 0));
        given(loadUserConnectionCount.lockAndLoad(PARTNER)).willReturn(UserConnectionCount.of(PARTNER, 0));

        UserConnection result = friendDisconnect.disconnect(SENDER, PARTNER_USERNAME);

        assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.DISCONNECTED);
        then(saveFriend).should().save(result);

        ArgumentCaptor<UserConnectionCount> captor = ArgumentCaptor.forClass(UserConnectionCount.class);
        then(saveUserConnectionCount).should(times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(count -> assertThat(count.getCount()).isEqualTo(0));
    }
}
