package net.study.messagesystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection;
import net.study.messagesystem.dto.domain.user.InviteCode;
import net.study.messagesystem.dto.domain.user.User;
import net.study.messagesystem.dto.domain.user.UserId;
import net.study.messagesystem.dto.projection.UserIdUsernameProjection;
import net.study.messagesystem.entity.user.UserEntity;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.repository.UserConnectionRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConnectionService {

    private final UserService userService;
    private final UserConnectionRepository userConnectionRepository;

    @Transactional
    public Pair<Optional<UserId>, String> invite(UserId inviterUserId, InviteCode inviteCode) {
        return userService.getUserIdName(inviteCode).or(Optional::empty)
                .filter(partner -> isNotSameUser(inviterUserId, partner.userId()))
                .flatMap(partner -> tryInvite(inviterUserId, partner))
                .orElseGet(() -> Pair.of(Optional.empty(), "Invite failed."));
    }

    @Transactional
    public Pair<Optional<UserId>, String> accept(UserId accepterUserId, String inviterUsername) {
        return userService.getUserId(inviterUsername).or(Optional::empty)
                .filter(inviterUserId -> isNotSameUser(accepterUserId, inviterUserId))
                .filter(inviterUserId -> isValidInvitation(accepterUserId, inviterUserId))
                .flatMap(inviterUserId -> tryConnect(accepterUserId, inviterUserId))
                .orElseGet(() -> Pair.of(Optional.empty(), "accept failed."));
    }

    @Transactional
    public Pair<Boolean, String> reject(UserId rejecterUserId, String inviterUsername) {
        return userService.getUserId(inviterUsername)
                .filter(inviterUserId -> isNotSameUser(rejecterUserId, inviterUserId))
                .filter(inviterUserId -> isValidInvitation(rejecterUserId, inviterUserId))
                .map(inviterUserId -> tryReject(rejecterUserId, inviterUserId, inviterUsername))
                .orElseGet(() -> Pair.of(false, "Reject failed"));
    }

    @Transactional
    public Pair<Boolean, String> disconnect(UserId senderUserId, String partnerUsername) {
        return userService.getUserId(partnerUsername).or(Optional::empty)
                .filter(partnerUserId -> isNotSameUser(senderUserId, partnerUserId))
                .map(partnerUserId -> tryDisconnect(senderUserId, partnerUserId, partnerUsername))
                .orElseGet(() -> Pair.of(false, "Disconnect failed"));
    }

    public List<User> getUsersByStatus(UserId userId, UserConnectionStatus status) {
        List<UserIdUsernameProjection> userA = userConnectionRepository.findByPartnerAUser_userIdAndStatus(userId.id(), status);
        List<UserIdUsernameProjection> userB = userConnectionRepository.findByPartnerBUser_userIdAndStatus(userId.id(), status);

        return Stream.concat(userA.stream(), userB.stream())
                .map(item -> new User(new UserId(item.getUserId()), item.getUsername()))
                .toList();
    }

    private boolean isNotSameUser(UserId userId1, UserId userId2) {
        if (userId1.equals(userId2)) {
            log.warn("User tried to perform action on themselves: {}", userId1);
            return false;
        }
        return true;
    }

    private Optional<Pair<Optional<UserId>, String>> tryInvite(UserId inviterUserId, User partner) {
        UserConnectionStatus connectionStatus = getConnectionStatus(inviterUserId, partner.userId());

        return switch (connectionStatus) {
            case NONE, DISCONNECTED -> Optional.of(processInvite(inviterUserId, partner.userId()));
            case PENDING, REJECTED  -> Optional.of(Pair.of(Optional.of(partner.userId()), "Already Invited to " + partner.username()));
            case ACCEPTED           -> Optional.of(Pair.of(Optional.of(partner.userId()), "Already connected with " + partner.username()));
        };
    }

    private boolean isValidInvitation(UserId accepterUserId, UserId inviterUserId) {
        return getTargetUser(accepterUserId, inviterUserId)
                .filter(invitationSenderUserId -> invitationSenderUserId.equals(inviterUserId))
                .isPresent();
    }

    private Optional<UserId> getTargetUser(UserId partnerAUserId, UserId partnerBUserId) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(partnerAUserId, partnerBUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();

        return userConnectionRepository.findInviterUserIdByPartnerAUser_userIdAndPartnerBUser_userId(firstUserId, secondUserId)
                .map(inviterUserId -> new UserId(inviterUserId.getInviterUserId()));
    }

    private UserConnectionStatus getConnectionStatus(UserId inviterUserId, UserId partnerUserId) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(inviterUserId, partnerUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();

        return userConnectionRepository
                .findUserConnectionStatusByPartnerAUser_userIdAndPartnerBUser_userId(firstUserId, secondUserId)
                .map(UserConnectionStatusProjection::getStatus)
                .map(UserConnectionStatus::valueOf)
                .orElse(UserConnectionStatus.NONE);
    }

    private Pair<Optional<UserId>, String> processInvite(UserId inviterUserId, UserId partnerUserId) {
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

    private Optional<Pair<Optional<UserId>, String>> tryConnect(UserId accepterUserId, UserId inviterUserId) {
        Optional<String> accepterUsername = userService.getUsername(accepterUserId);
        if (accepterUsername.isEmpty()) {
            log.error("Invalid userId: {}", accepterUserId);
            return Optional.empty();
        }

        try {
            this.connect(accepterUserId, inviterUserId);
            return Optional.of(Pair.of(Optional.of(inviterUserId), accepterUsername.get()));
        } catch (EntityNotFoundException e) {
            log.error("accept failed. cause: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalStateException e) {
            return Optional.of(Pair.of(Optional.empty(), e.getMessage()));
        }
    }

    private Pair<Boolean, String> tryReject(UserId rejecterUserId, UserId inviterUserId, String inviterUsername) {
        try {
            reject(rejecterUserId, inviterUserId);
            return Pair.of(true, inviterUsername);
        } catch (Exception ex) {
            log.error("reject failed. cause: {}", ex.getMessage());
            return Pair.of(false, "Reject failed.");
        }
    }

    private Pair<Boolean, String> tryDisconnect(UserId senderUserId, UserId partnerUserId, String partnerUsername) {
        try {
            disconnect(senderUserId, partnerUserId);
            return Pair.of(true, partnerUsername);
        } catch (Exception ex) {
            log.error("disconnect failed. cause: {}", ex.getMessage());
            return Pair.of(false, "Disconnect failed");
        }
    }

    private void disconnect(UserId senderUserId, UserId partnerUserId) {
        UserConnectionEntity connectionEntity = getConnectionEntity(senderUserId, partnerUserId, UserConnectionStatus.ACCEPTED);
        connectionEntity.disconnect();
    }

    private void connect(UserId accepterUserId, UserId inviterUserId) {
        UserConnectionEntity connectionEntity = getConnectionEntity(accepterUserId, inviterUserId, UserConnectionStatus.PENDING);
        connectionEntity.connect();
    }

    private void reject(UserId rejecterUserId, UserId inviterUserId) {
        UserConnectionEntity connectionEntity = getConnectionEntity(rejecterUserId, inviterUserId, UserConnectionStatus.PENDING);
        connectionEntity.reject();
    }

    private UserConnectionEntity getConnectionEntity(UserId accepterUserId, UserId inviterUserId, UserConnectionStatus status) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(accepterUserId, inviterUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();

        return userConnectionRepository
                .findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(firstUserId, secondUserId, status)
                .orElseThrow(() -> new EntityNotFoundException("Invalid status"));
    }

    private Pair<Long, Long> getUserIdAscending(UserId userAId, UserId userBId) {
        return Pair.of(Math.min(userAId.id(), userBId.id()),
                       Math.max(userAId.id(), userBId.id()));
    }
}
