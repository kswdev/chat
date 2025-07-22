package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection;
import net.study.messagesystem.dto.user.InviteCode;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.dto.user.User;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.repository.UserConnectionRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConnectionService {

    private final UserService userService;
    private final UserConnectionRepository userConnectionRepository;

    public Pair<Optional<UserId>, String> invite(UserId inviterUserId, InviteCode inviteCode) {
        Optional<User> partner = userService.getUserIdName(inviteCode);
        
        if (partner.isEmpty()) {
            log.warn("User not found: {}", inviteCode.code());
            return Pair.of(Optional.empty(), "User not found");
        }

        UserId partnerUserId = partner.get().userId();
        String partnerUsername = partner.get().username();

        if (inviterUserId.equals(partnerUserId)) {
            log.warn("Cannot invite self: {}", partnerUserId);
            return Pair.of(Optional.empty(), "Cannot invite self");
        }

        UserConnectionStatus connectionStatus = getConnectionStatus(inviterUserId, partnerUserId);

        return switch (connectionStatus) {
            case NONE, DISCONNECTED -> handleInvite(inviterUserId, partnerUserId);
            case PENDING, REJECTED  -> Pair.of(Optional.of(partnerUserId), "Already Invited to " + partnerUsername);
            case CONNECTED          -> Pair.of(Optional.of(partnerUserId), "Already connected with " + partnerUsername);
        };
    }

    private UserConnectionStatus getConnectionStatus(UserId inviterUserId, UserId partnerUserId) {
        return userConnectionRepository
                .findByPartnerAUserIdAndPartnerBUserId(
                        Math.min(inviterUserId.id(), partnerUserId.id()),
                        Math.max(inviterUserId.id(), partnerUserId.id())
                )
                .map(UserConnectionStatusProjection::getStatus)
                .map(UserConnectionStatus::valueOf)
                .orElse(UserConnectionStatus.NONE);
    }

    private Pair<Optional<UserId>, String>  handleInvite(UserId inviterUserId, UserId partnerUserId) {
        Optional<String> username = userService.getUsername(inviterUserId);

        if (username.isEmpty()) {
            log.warn("inviter not found: {}", inviterUserId);
            return Pair.of(Optional.empty(), "User not found");
        }

        UserConnectionEntity userConnection = UserConnectionEntity.create(
                inviterUserId, partnerUserId, inviterUserId.id()
        );

        userConnectionRepository.save(userConnection);

        return Pair.of(Optional.of(partnerUserId), username.get());
    }
}
