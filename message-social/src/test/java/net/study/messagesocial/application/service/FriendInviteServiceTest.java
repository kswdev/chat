package net.study.messagesocial.application.service;

import net.study.messagesocial.adapter.in.web.exception.AlreadyConnectedException;
import net.study.messagesocial.adapter.in.web.exception.AlreadyInvitedException;
import net.study.messagesocial.adapter.in.web.exception.InvalidInviteCodeException;
import net.study.messagesocial.application.port.out.LoadUserConnectionPort;
import net.study.messagesocial.application.port.out.LoadUserPort;
import net.study.messagesocial.application.port.out.SaveFriendPort;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
class FriendInviteServiceTest {

    @InjectMocks
    private FriendInviteService friendInvite;

    @Mock
    private LoadUserPort loadUser;

    @Mock
    private SaveFriendPort saveFriend;

    @Mock
    private LoadUserConnectionPort loadUserConnection;


    @Test
    void  invite_with_unknown_code_throws_and_does_not_save() {
        //given
        Long inviter = 1L;
        String inviteCode = "ABC123";
        given(loadUser.getUserIdByInviteCode(inviteCode))
                .willReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> friendInvite.invite(inviter, inviteCode));

        //then
        then(saveFriend).should(never()).save(any());
        assertThat(thrown).isInstanceOf(InvalidInviteCodeException.class);

    }

    @ParameterizedTest
    @MethodSource("inviteDataProvider")
    void invite_with_valid_code_should_pass(Long inviter, Long invitee, String inviteCode) {
        //given
        given(loadUser.getUserIdByInviteCode(inviteCode))
                .willReturn(Optional.of(invitee));
        given(loadUserConnection.getUserConnection(inviter, invitee))
                .willReturn(Optional.empty());

        //when
        UserConnection connection = friendInvite.invite(inviter, inviteCode);

        //then
        then(saveFriend).should().save(any());
        assertThat(connection.getInviterId()).isEqualTo(inviter);
        assertThat(connection.getInviteeId()).isEqualTo(invitee);
        assertThat(connection.getStatus()).isEqualTo(UserConnectionStatus.PENDING);
    }

    private static Stream<Arguments> inviteDataProvider() {
        return Stream.of(
                Arguments.of(1L, 2L, "ABC123"),
                Arguments.of(1L, 3L, "ABC133"),
                Arguments.of(2L, 5L, "ABC155")
        );
    }

    @ParameterizedTest
    @MethodSource("inviteDataProviderWithStatus1")
    void invite_save_connection_when_connection_status_is_none_and_disconnected(
            Long inviter,
            Long invitee,
            String inviteCode,
            UserConnection userConnection
    ) {
        //given
        given(loadUser.getUserIdByInviteCode(inviteCode))
                .willReturn(Optional.of(invitee));
        given(loadUserConnection.getUserConnection(inviter, invitee))
                .willReturn(Optional.ofNullable(userConnection));

        //when
        UserConnection connection = friendInvite.invite(inviter, inviteCode);

        //then
        then(saveFriend).should().save(any());
        assertThat(connection.getInviterId()).isEqualTo(inviter);
        assertThat(connection.getInviteeId()).isEqualTo(invitee);
        assertThat(connection.getStatus()).isEqualTo(UserConnectionStatus.PENDING);
    }

    private static Stream<Arguments> inviteDataProviderWithStatus1() {
        return Stream.of(
                Arguments.of(1L, 2L, "ABC123", null),
                Arguments.of(1L, 3L, "ABC133", UserConnection.builder()
                        .inviterId(1L)
                        .inviteeId(3L)
                        .status(UserConnectionStatus.NONE)
                        .build()),
                Arguments.of(1L, 3L, "ABC133", UserConnection.builder()
                        .inviterId(1L)
                        .inviteeId(3L)
                        .status(UserConnectionStatus.DISCONNECTED)
                        .build()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("inviteDataProviderWithStatus2")
    void invite_does_not_save_connection_when_connection_status_is_not_none_and_disconnected(
            Long inviter,
            Long invitee,
            String inviteCode,
            UserConnection userConnection,
            Class<RuntimeException> exception
    ) {
        //given
        given(loadUser.getUserIdByInviteCode(inviteCode))
                .willReturn(Optional.of(invitee));
        given(loadUserConnection.getUserConnection(inviter, invitee))
                .willReturn(Optional.of(userConnection));

        //when
        Throwable thrown = catchThrowable(() -> friendInvite.invite(inviter, inviteCode));

        //then
        then(saveFriend).should(never()).save(any());
        assertThat(thrown).isInstanceOf(exception);
    }

    private static Stream<Arguments> inviteDataProviderWithStatus2() {
        return Stream.of(
                Arguments.of(1L, 2L, "ABC123", UserConnection.builder()
                                .inviterId(1L)
                                .inviteeId(2L)
                                .status(UserConnectionStatus.ACCEPTED)
                                .build(),
                                AlreadyConnectedException.class),
                Arguments.of(1L, 2L, "ABC133", UserConnection.builder()
                                .inviterId(1L)
                                .inviteeId(2L)
                                .status(UserConnectionStatus.PENDING)
                                .build(),
                                AlreadyInvitedException.class),
                Arguments.of(1L, 2L, "ABC133", UserConnection.builder()
                                .inviterId(1L)
                                .inviteeId(2L)
                                .status(UserConnectionStatus.REJECTED)
                                .build(),
                                AlreadyInvitedException.class)
        );
    }
}