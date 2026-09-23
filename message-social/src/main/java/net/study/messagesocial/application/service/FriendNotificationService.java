package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.adapter.out.notification.dto.AcceptNotificationRecord;
import net.study.messagesocial.adapter.out.notification.PushService;
import net.study.messagesocial.adapter.out.notification.dto.InviteNotificationRecord;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendNotificationService {

    private final PushService pushService;

    public void notifyInvite(Long recipientId, String actorUsername) {
        pushService.pushMessage(new InviteNotificationRecord(recipientId, actorUsername));
    }

    public void notifyAccept(Long recipientId, String actorUsername) {
        pushService.pushMessage(new AcceptNotificationRecord(recipientId, actorUsername));
    }
}
