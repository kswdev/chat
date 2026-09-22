package net.study.messagesocial.application.service;

import lombok.RequiredArgsConstructor;
import net.study.messagesocial.adapter.out.notification.dto.AcceptNotificationRecord;
import net.study.messagesocial.adapter.out.notification.PushService;
import net.study.messagesocial.adapter.out.notification.dto.InviteNotificationRecord;
import net.study.messagesocial.application.port.out.LoadUserPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendNotificationService {

    private final LoadUserPort loadUser;
    private final PushService pushService;

    public void notifyInvite(Long recipientId, Long actorId) {
        loadUser.getUsername(actorId).ifPresent(actorUsername ->
                pushService.pushMessage(new InviteNotificationRecord(recipientId, actorUsername)));
    }

    public void notifyAccept(Long recipientId, Long actorId) {
        loadUser.getUsername(actorId).ifPresent(actorUsername ->
                pushService.pushMessage(new AcceptNotificationRecord(recipientId, actorUsername)));
    }
}
