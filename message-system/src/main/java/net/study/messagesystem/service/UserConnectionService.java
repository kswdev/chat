package net.study.messagesystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection;
import net.study.messagesystem.dto.user.InviteCode;
import net.study.messagesystem.dto.user.User;
import net.study.messagesystem.dto.user.UserId;
import net.study.messagesystem.entity.user.UserEntity;
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
    private final UserConnectionLimitService userConnectionLimitService;
    private final UserConnectionRepository userConnectionRepository;

    public Pair<Optional<UserId>, String> invite(UserId inviterUserId, InviteCode inviteCode) {
        return getInviterUser(inviteCode)
                .filter(partner -> isNotInviteSameUser(inviterUserId, partner.userId()) )
                .flatMap(partner -> tryInvite(inviterUserId, partner))
                .orElseGet(() -> Pair.of(Optional.empty(), "Cannot invite self"));
    }

    private boolean isNotInviteSameUser(UserId inviterUserId, UserId partnerUserId) {
        if (inviterUserId.equals(partnerUserId)) {
            log.warn("User tried to invite themselves: {}", inviterUserId);
            return false;
        }
        return true;
    }

    private Optional<User> getInviterUser(InviteCode inviteCode) {
        return userService.getUserIdName(inviteCode)
                .or(() -> {
                    log.warn("User not found: {}", inviteCode.code());
                    return Optional.empty();
                });
    }

    private Optional<Pair<Optional<UserId>, String>> tryInvite(UserId inviterUserId, User partner) {
        UserConnectionStatus connectionStatus = getConnectionStatus(inviterUserId, partner.userId());

        return switch (connectionStatus) {
            case NONE, DISCONNECTED -> Optional.of(handleInvite(inviterUserId, partner.userId()));
            case PENDING, REJECTED  -> Optional.of(Pair.of(Optional.of(partner.userId()), "Already Invited to " + partner.username()));
            case ACCEPTED           -> Optional.of(Pair.of(Optional.of(partner.userId()), "Already connected with " + partner.username()));
        };
    }

    public Pair<Optional<UserId>, String> accept(UserId accepterUserId, String inviterUsername) {
        return getInviterUserId(inviterUsername)
                .filter(inviterUserId -> isNotAcceptSameUser(accepterUserId, inviterUserId))
                .filter(inviterUserId -> isValidInvitation(accepterUserId, inviterUserId))
                .filter(inviterUserId -> isPendingStatus(inviterUserId, accepterUserId))
                .flatMap(inviterUserId -> tryConnect(accepterUserId, inviterUserId))
                .orElseGet(() -> Pair.of(Optional.empty(), "accept failed"));
    }

    private Optional<UserId> getInviterUserId(String inviterUsername) {
        return userService.getUserId(inviterUsername)
                .or(() -> {
                    log.warn("Invalid username: {}", inviterUsername);
                    return Optional.empty();
                });
    }

    private boolean isNotAcceptSameUser(UserId accepterUserId, UserId inviterUserId) {
        if (accepterUserId.equals(inviterUserId)) {
            log.warn("User tried to accept themselves: {}", accepterUserId);
            return false;
        }
        return true;
    }

    private boolean isValidInvitation(UserId accepterUserId, UserId inviterUserId) {
        return getInviteUserId(accepterUserId, inviterUserId)
                .filter(invitationSenderUserId -> invitationSenderUserId.equals(inviterUserId))
                .isPresent();
    }

    private boolean isPendingStatus(UserId inviterUserId, UserId accepterUserId) {
        UserConnectionStatus status = getConnectionStatus(inviterUserId, accepterUserId);
        if (status == UserConnectionStatus.ACCEPTED) {
            log.warn("Already connected: {} <-> {}", inviterUserId, accepterUserId);
            return false;
        }
        return status == UserConnectionStatus.PENDING;
    }

    private Optional<Pair<Optional<UserId>, String>> tryConnect(UserId accepterUserId, UserId inviterUserId) {
        Optional<String> accepterUsername = userService.getUsername(accepterUserId);
        if (accepterUsername.isEmpty()) {
            log.error("Invalid userId: {}", accepterUserId);
            return Optional.empty();
        }

        try {
            userConnectionLimitService.connect(accepterUserId, inviterUserId);
            return Optional.of(Pair.of(Optional.of(inviterUserId), accepterUsername.get()));
        } catch (EntityNotFoundException e) {
            log.error("accept failed. cause: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalStateException e) {
            return Optional.of(Pair.of(Optional.empty(), e.getMessage()));
        }
    }

    private Optional<UserId> getInviteUserId(UserId partnerAUserId, UserId partnerBUserId) {
        return userConnectionRepository.findInviterUserIdByPartnerAUser_userIdAndPartnerAUser_userId(
                Math.min(partnerAUserId.id(), partnerBUserId.id()),
                Math.max(partnerAUserId.id(), partnerBUserId.id())
        ).map(inviterUserId -> new UserId(inviterUserId.getInviterUserId()));
    }

    private UserConnectionStatus getConnectionStatus(UserId inviterUserId, UserId partnerUserId) {
        return userConnectionRepository
                .findUserConnectionStatusByPartnerAUser_userIdAndPartnerBUser_userId(
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

        UserEntity inviter = userService.getUserReference(inviterUserId);
        UserEntity partner = userService.getUserReference(partnerUserId);

        UserConnectionEntity userConnection = UserConnectionEntity.create(
                inviter, partner, inviterUserId.id()
        );

        userConnectionRepository.save(userConnection);

        return Pair.of(Optional.of(partnerUserId), username.get());
    }
}
