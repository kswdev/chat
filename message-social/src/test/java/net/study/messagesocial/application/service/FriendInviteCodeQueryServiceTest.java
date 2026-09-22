package net.study.messagesocial.application.service;

import net.study.messagesocial.adapter.in.web.exception.UserNotFoundException;
import net.study.messagesocial.application.port.out.LoadUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FriendInviteCodeQueryServiceTest {

    @InjectMocks
    private FriendInviteCodeQueryService friendInviteCodeQuery;

    @Mock
    private LoadUserPort loadUser;

    @Test
    void getInviteCode_returns_code_when_user_exists() {
        given(loadUser.getInviteCode(1L)).willReturn(Optional.of("ABC123"));

        String result = friendInviteCodeQuery.getInviteCode(1L);

        assertThat(result).isEqualTo("ABC123");
    }

    @Test
    void getInviteCode_throws_when_user_not_found() {
        given(loadUser.getInviteCode(1L)).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> friendInviteCodeQuery.getInviteCode(1L));

        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
    }
}
