package net.study.messagesocial.adapter.in.web.api;

import lombok.RequiredArgsConstructor;
import net.study.messagecommon.constant.IdKey;
import net.study.messagesocial.adapter.in.web.dto.response.ConnectionAcceptedCountResponse;
import net.study.messagesocial.adapter.in.web.dto.response.ConnectionsResponse;
import net.study.messagesocial.adapter.in.web.dto.response.InviteCodeResponse;
import net.study.messagesocial.adapter.in.web.dto.response.InviteResponse;
import net.study.messagesocial.application.port.in.*;
import net.study.messagesocial.application.service.FriendNotificationService;
import net.study.messagesocial.domain.userconnection.FriendConnectionSummary;
import net.study.messagesocial.domain.userconnection.UserConnection;
import net.study.messagesocial.domain.userconnection.UserConnectionStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/social/friends")
public class FriendController {

    private final FriendInvite friendInvite;
    private final FriendAccept friendAccept;
    private final FriendReject friendReject;
    private final FriendDisconnect friendDisconnect;
    private final FriendConnectionQuery friendConnectionQuery;
    private final FriendInviteCodeQuery friendInviteCodeQuery;
    private final FriendNotificationService friendNotificationService;

    @PostMapping("/invite/{inviteCode}")
    public ResponseEntity<InviteResponse> invite(
            @PathVariable String inviteCode,
            @RequestHeader HttpHeaders headers
    ) {
        Long userId = currentUserId(headers);
        String username = currentUsername(headers);
        UserConnection userConnection = friendInvite.invite(userId, inviteCode);

        friendNotificationService.notifyInvite(userConnection.getInviteeId(), username);

        return ResponseEntity.ok(toResponse(userConnection));
    }

    @PostMapping("/accept/{username}")
    public ResponseEntity<InviteResponse> accept(
            @PathVariable String username,
            @RequestHeader HttpHeaders headers
    ) {
        Long userId = currentUserId(headers);
        String accepterUsername = currentUsername(headers);
        UserConnection userConnection = friendAccept.accept(userId, username);

        friendNotificationService.notifyAccept(userConnection.getInviterId(), accepterUsername);

        return ResponseEntity.ok(toResponse(userConnection));
    }

    @PostMapping("/reject/{username}")
    public ResponseEntity<InviteResponse> reject(
            @PathVariable String username,
            @RequestHeader HttpHeaders headers
    ) {
        Long userId = currentUserId(headers);
        UserConnection userConnection = friendReject.reject(userId, username);

        return ResponseEntity.ok(toResponse(userConnection));
    }

    @PostMapping("/disconnect/{username}")
    public ResponseEntity<InviteResponse> disconnect(
            @PathVariable String username,
            @RequestHeader HttpHeaders headers
    ) {
        Long userId = currentUserId(headers);
        UserConnection userConnection = friendDisconnect.disconnect(userId, username);

        return ResponseEntity.ok(toResponse(userConnection));
    }

    @GetMapping("/connections")
    public ResponseEntity<ConnectionsResponse> connections(
            @RequestParam UserConnectionStatus status,
            @RequestHeader HttpHeaders headers
    ) {
        Long userId = currentUserId(headers);
        List<FriendConnectionSummary> connections = friendConnectionQuery.getConnections(userId, status);

        List<ConnectionsResponse.ConnectionSummary> summaries = connections.stream()
                .map(connection -> new ConnectionsResponse.ConnectionSummary(
                        connection.partnerId(), connection.partnerUsername(), connection.status().name()))
                .toList();

        return ResponseEntity.ok(new ConnectionsResponse(summaries));
    }

    @GetMapping("/invite-code")
    public ResponseEntity<InviteCodeResponse> inviteCode(@RequestHeader HttpHeaders headers) {
        Long userId = currentUserId(headers);
        String inviteCode = friendInviteCodeQuery.getInviteCode(userId);

        return ResponseEntity.ok(new InviteCodeResponse(inviteCode));
    }

    /**
     * 서비스 간 내부 호출 전용(web-gateway를 거치지 않음) — message-system이 채널 생성 시
     * 참여자들의 친구 연결 여부를 확인하기 위해 직접 호출한다. userId를 헤더가 아닌 쿼리 파라미터로 받는다.
     * {@link net.study.messagesocial.adapter.in.web.filter.InternalApiKeyFilter}가 API 키를 검증한다.
     */
    @GetMapping("/connections/accepted-count")
    public ResponseEntity<ConnectionAcceptedCountResponse> acceptedConnectionCount(
            @RequestParam Long userId,
            @RequestParam List<Long> partnerIds
    ) {
        long count = friendConnectionQuery.countAccepted(userId, partnerIds);

        return ResponseEntity.ok(new ConnectionAcceptedCountResponse(count));
    }

    private Long currentUserId(HttpHeaders headers) {
        return Long.valueOf(headers.getFirst(IdKey.USER_ID.getValue()));
    }

    private String currentUsername(HttpHeaders headers) {
        return headers.getFirst(IdKey.USERNAME.getValue());
    }

    private InviteResponse toResponse(UserConnection userConnection) {
        return new InviteResponse(
                userConnection.getInviterId(),
                userConnection.getInviteeId(),
                userConnection.getStatus().name());
    }
}
