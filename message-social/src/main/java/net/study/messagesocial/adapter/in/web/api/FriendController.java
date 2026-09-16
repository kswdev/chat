package net.study.messagesocial.adapter.in.web.api;

import lombok.RequiredArgsConstructor;
import net.study.messagecommon.constant.IdKey;
import net.study.messagesocial.adapter.in.web.dto.response.InviteResponse;
import net.study.messagesocial.application.port.in.FriendInvite;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/social/friends")
public class FriendController {

    private final FriendInvite friendInvite;

    private static final String PENDING = "PENDING";

    @PostMapping("/invite/{inviteCode}")
    public ResponseEntity<InviteResponse> invite(
            @PathVariable String inviteCode,
            @RequestHeader HttpHeaders headers
    ) {
        Long userId = Long.valueOf(headers.getFirst(IdKey.USER_ID.getValue()));
        friendInvite.invite(userId, inviteCode);
        return ResponseEntity.ok(new InviteResponse(userId, inviteCode, PENDING));
    }
}
