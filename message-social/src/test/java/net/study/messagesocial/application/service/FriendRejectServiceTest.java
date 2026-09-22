
package net.study.messagesocial.application.service;

import net.study.messagesocial.adapter.in.web.exception.ConnectionNotFoundException;
import net.study.messagesocial.adapter.in.web.exception.InvalidConnectionStatusException;
import net.study.messagesocial.adapter.in.web.exception.UserNotFoundException;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FriendRejectServiceTest {

    @InjectMocks
    private FriendRejectService friendReject;

    @Mock
    private LoadUserPort loadUser;

    @Mock
    private SaveFriendPort saveFriend;

    @Mock
    private LoadUserConnectionPort loadUserConnection;

    private static final Long INVITER = 1L;
    private static final Long REJECTER = 2L;
    private static final String INVITER_USERNAME = "inviter";

    @Test
    void reject_throws_when_inviter_username_not_found() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> friendReject.reject(REJECTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        then(saveFriend).should(never()).save(any());
    }

    @Test
    void reject_throws_when_no_connection_exists() {
        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, REJECTER)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> friendReject.reject(REJECTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(ConnectionNotFoundException.class);
        then(saveFriend).should(never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = UserConnectionStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
    void reject_throws_when_connection_is_not_pending(UserConnectionStatus status) {
        UserConnection connection = UserConnection.builder()
                .inviterId(INVITER).inviteeId(REJECTER).status(status).build();

        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, REJECTER)).willReturn(Optional.of(connection));

        Throwable thrown = catchThrowable(() -> friendReject.reject(REJECTER, INVITER_USERNAME));

        assertThat(thrown).isInstanceOf(InvalidConnectionStatusException.class);
        then(saveFriend).should(never()).save(any());
    }

    @Test
    void reject_saves_rejected_connection_when_pending() {
        UserConnection connection = UserConnection.builder()
                .inviterId(INVITER).inviteeId(REJECTER).status(UserConnectionStatus.PENDING).build();

        given(loadUser.getUserIdByUsername(INVITER_USERNAME)).willReturn(Optional.of(INVITER));
        given(loadUserConnection.getUserConnection(INVITER, REJECTER)).willReturn(Optional.of(connection));

        UserConnection result = friendReject.reject(REJECTER, INVITER_USERNAME);

        assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.REJECTED);
        then(saveFriend).should().save(result);
    }
}
