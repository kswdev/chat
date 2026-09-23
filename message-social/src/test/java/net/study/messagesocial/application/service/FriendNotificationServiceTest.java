package net.study.messagesocial.application.service;

import net.study.messagesocial.adapter.out.notification.dto.AcceptNotificationRecord;
import net.study.messagesocial.adapter.out.notification.PushService;
import net.study.messagesocial.adapter.out.notification.dto.InviteNotificationRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FriendNotificationServiceTest {

    @InjectMocks
    private FriendNotificationService friendNotificationService;

    @Mock
    private PushService pushService;

    private static final Long RECIPIENT_ID = 1L;
    private static final String ACTOR_USERNAME = "inviter";

    @Test
    void notifyInvite_publishes_invite_event_targeted_at_recipient_with_actor_username() {
        friendNotificationService.notifyInvite(RECIPIENT_ID, ACTOR_USERNAME);

        then(pushService).should()
                .pushMessage(new InviteNotificationRecord(RECIPIENT_ID, ACTOR_USERNAME));
    }

    @Test
    void notifyAccept_publishes_accept_event_targeted_at_recipient_with_actor_username() {
        friendNotificationService.notifyAccept(RECIPIENT_ID, ACTOR_USERNAME);

        then(pushService).should()
                .pushMessage(new AcceptNotificationRecord(RECIPIENT_ID, ACTOR_USERNAME));
    }
}
