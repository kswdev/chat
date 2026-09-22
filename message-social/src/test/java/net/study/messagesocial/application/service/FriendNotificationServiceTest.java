package net.study.messagesocial.application.service;

import net.study.messagesocial.adapter.out.notification.dto.AcceptNotificationRecord;
import net.study.messagesocial.adapter.out.notification.PushService;
import net.study.messagesocial.adapter.out.notification.dto.InviteNotificationRecord;
import net.study.messagesocial.application.port.out.LoadUserPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FriendNotificationServiceTest {

    @InjectMocks
    private FriendNotificationService friendNotificationService;

    @Mock
    private LoadUserPort loadUser;

    @Mock
    private PushService pushService;

    private static final Long RECIPIENT_ID = 1L;
    private static final Long ACTOR_ID = 2L;

    @Test
    void notifyInvite_publishes_invite_event_targeted_at_recipient_with_actor_username() {
        given(loadUser.getUsername(ACTOR_ID)).willReturn(Optional.of("inviter"));

        friendNotificationService.notifyInvite(RECIPIENT_ID, ACTOR_ID);

        then(pushService).should()
                .pushMessage(new InviteNotificationRecord(RECIPIENT_ID, "inviter"));
    }

    @Test
    void notifyInvite_does_not_publish_when_actor_username_is_unknown() {
        given(loadUser.getUsername(ACTOR_ID)).willReturn(Optional.empty());

        friendNotificationService.notifyInvite(RECIPIENT_ID, ACTOR_ID);

        then(pushService).should(never()).pushMessage(any());
    }

    @Test
    void notifyAccept_publishes_accept_event_targeted_at_recipient_with_actor_username() {
        given(loadUser.getUsername(ACTOR_ID)).willReturn(Optional.of("accepter"));

        friendNotificationService.notifyAccept(RECIPIENT_ID, ACTOR_ID);

        then(pushService).should()
                .pushMessage(new AcceptNotificationRecord(RECIPIENT_ID, "accepter"));
    }

    @Test
    void notifyAccept_does_not_publish_when_actor_username_is_unknown() {
        given(loadUser.getUsername(ACTOR_ID)).willReturn(Optional.empty());

        friendNotificationService.notifyAccept(RECIPIENT_ID, ACTOR_ID);

        then(pushService).should(never()).pushMessage(any());
    }
}
