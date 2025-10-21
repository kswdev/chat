package net.study.messagesystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.constant.KeyPrefix;
import net.study.messagesystem.constant.UserConnectionStatus;
import net.study.messagesystem.domain.user.InviteCode;
import net.study.messagesystem.domain.user.User;
import net.study.messagesystem.domain.user.UserId;
import net.study.messagesystem.dto.projection.InviterUserIdProjection;
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection;
import net.study.messagesystem.dto.projection.UserIdUsernameInviterUserIdProjection;
import net.study.messagesystem.entity.user.UserEntity;
import net.study.messagesystem.entity.user.connection.UserConnectionEntity;
import net.study.messagesystem.repository.connection.UserConnectionRepository;
import net.study.messagesystem.util.JsonUtil;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConnectionService {

    private final Long TTL = 600L;

    private final JsonUtil jsonUtil;
    private final UserService userService;
    private final CacheService cacheService;
    private final UserConnectionLimitService userConnectionLimitService;

    private final UserConnectionRepository userConnectionRepository;

    @Transactional
    public Pair<Optional<UserId>, String> invite(UserId inviterUserId, InviteCode inviteCode) {
        return userService.getUserIdName(inviteCode).or(Optional::empty)
                .filter(partner -> isNotSameUser(inviterUserId, partner.userId()))
                .map(partner -> tryInvite(inviterUserId, partner))
                .orElseGet(() -> Pair.of(Optional.empty(), "Invite failed."));
    }

    @Transactional
    public Pair<Optional<UserId>, String> accept(UserId accepterUserId, String inviterUsername) {
        return userService.getUserId(inviterUsername).or(Optional::empty)
                .filter(inviterUserId -> isNotSameUser(accepterUserId, inviterUserId))
                .filter(inviterUserId -> isValidInvitation(accepterUserId, inviterUserId))
                .map(inviterUserId -> tryConnect(accepterUserId, inviterUserId))
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

    @Transactional(readOnly = true)
    public List<User> getUsersByStatus(UserId userId, UserConnectionStatus status) {
        String key = cacheService.buildKey(KeyPrefix.CONNECTIONS_STATUS, userId.id().toString(), status.name());

        Optional<String> cachedUser = cacheService.get(key);

        if (cachedUser.isPresent()) {
            return jsonUtil.fromJsonToList(cachedUser.get(), User.class);
        }

        List<UserIdUsernameInviterUserIdProjection> userA = userConnectionRepository.findByPartnerAUser_userIdAndStatus(userId.id(), status);
        List<UserIdUsernameInviterUserIdProjection> userB = userConnectionRepository.findByPartnerBUser_userIdAndStatus(userId.id(), status);

        List<User> fromDb;
        if (status == UserConnectionStatus.ACCEPTED)
            fromDb = Stream.concat(userA.stream(), userB.stream())
                    .map(item -> new User(new UserId(item.getUserId()), item.getUsername()))
                    .toList();
        else
            fromDb = Stream.concat(userA.stream(), userB.stream())
                    .filter(item -> !item.getInviterUserId().equals(userId.id()))
                    .map(item -> new User(new UserId(item.getUserId()), item.getUsername()))
                    .toList();

        if (!fromDb.isEmpty())
            jsonUtil.toJson(fromDb)
                    .ifPresent((json) -> cacheService.set(key, json, TTL));


        return fromDb;
    }

    @Transactional(readOnly = true)
    public long countConnectionStatus(UserId userId, List<UserId> partnerUserIds, UserConnectionStatus status) {
        List<Long> ids = partnerUserIds.stream()
                .map(UserId::id)
                .toList();

        long countA = userConnectionRepository.countByPartnerAUser_UserIdAndPartnerBUser_userIdInAndStatus(userId.id(), ids, status);
        long countB = userConnectionRepository.countByPartnerBUser_userIdAndPartnerAUser_UserIdInAndStatus(userId.id(), ids, status);

        return countA + countB;
    }

    private boolean isNotSameUser(UserId userId1, UserId userId2) {
        if (userId1.equals(userId2)) {
            log.warn("User tried to perform action on themselves: {}", userId1);
            return false;
        }
        return true;
    }

    private Pair<Optional<UserId>, String> tryInvite(UserId inviterUserId, User partner) {
        UserConnectionStatus connectionStatus = getConnectionStatus(inviterUserId, partner.userId());

        return switch (connectionStatus) {
            case NONE, DISCONNECTED -> processInvite(inviterUserId, partner.userId());
            case PENDING, REJECTED  -> Pair.of(Optional.of(partner.userId()), "Already Invited to " + partner.username());
            case ACCEPTED           -> Pair.of(Optional.of(partner.userId()), "Already connected with " + partner.username());
        };
    }

    private boolean isValidInvitation(UserId accepterUserId, UserId inviterUserId) {
        return getInviterUserId(accepterUserId, inviterUserId)
                .filter(invitationSenderUserId -> invitationSenderUserId.equals(inviterUserId))
                .isPresent();
    }

    private Optional<UserId> getInviterUserId(UserId partnerAUserId, UserId partnerBUserId) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(partnerAUserId, partnerBUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();

        String key = cacheService.buildKey(KeyPrefix.INVITER_USER_ID, firstUserId.toString(), secondUserId.toString());

        return cacheService.get(key)
                .map(Long::valueOf)
                .map(UserId::new)
                .or(() -> userConnectionRepository
                        .findInviterUserIdByPartnerAUser_userIdAndPartnerBUser_userId(firstUserId, secondUserId)
                        .map(InviterUserIdProjection::getInviterUserId)
                        .map(userId -> {
                            cacheService.set(key, userId.toString(), TTL);
                            return new UserId(userId);
                        }));
    }

    private UserConnectionStatus getConnectionStatus(UserId inviterUserId, UserId partnerUserId) {
        Pair<Long, Long> userIdAscending = getUserIdAscending(inviterUserId, partnerUserId);
        Long firstUserId = userIdAscending.getFirst();
        Long secondUserId = userIdAscending.getSecond();

        String key = cacheService.buildKey(KeyPrefix.CONNECTION_STATUS, firstUserId.toString(), secondUserId.toString());

        return cacheService.get(key)
                .map(UserConnectionStatus::valueOf)
                .or(() -> userConnectionRepository
                        .findUserConnectionStatusByPartnerAUser_userIdAndPartnerBUser_userId(firstUserId, secondUserId)
                        .map(UserConnectionStatusProjection::getStatus)
                        .map(fromDb -> {
                            cacheService.set(key, fromDb, TTL);
                            return UserConnectionStatus.valueOf(fromDb);
                        }))
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

    private Pair<Optional<UserId>, String> tryConnect(UserId accepterUserId, UserId inviterUserId) {
        Optional<String> accepterUsername = userService.getUsername(accepterUserId);
        if (accepterUsername.isEmpty()) {
            log.error("Invalid userId: {}", accepterUserId);
            return Pair.of(Optional.empty(), "Invalid userId");
        }

        try {
            userConnectionLimitService.connect(accepterUserId, inviterUserId);
            return Pair.of(Optional.of(inviterUserId), accepterUsername.get());
        } catch (IllegalStateException e) {
            if (TransactionSynchronizationManager.isActualTransactionActive())
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Pair.of(Optional.empty(), e.getMessage());
        } catch (Exception e) {
            if (TransactionSynchronizationManager.isActualTransactionActive())
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("accept failed. cause: {}", e.getMessage());
            return Pair.of(Optional.empty(), "Invalid status or userId.");
        }
    }

    private Pair<Boolean, String> tryReject(UserId rejecterUserId, UserId inviterUserId, String inviterUsername) {
        try {
            userConnectionLimitService.reject(rejecterUserId, inviterUserId);
            return Pair.of(true, inviterUsername);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("reject failed. cause: {}", ex.getMessage());
            return Pair.of(false, "Reject failed.");
        }
    }

    protected Pair<Boolean, String> tryDisconnect(UserId senderUserId, UserId partnerUserId, String partnerUsername) {
        try {
            userConnectionLimitService.disconnect(senderUserId, partnerUserId);
            return Pair.of(true, partnerUsername);
        } catch (Exception ex) {
            if (TransactionSynchronizationManager.isActualTransactionActive())
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("disconnect failed. cause: {}", ex.getMessage());
            return Pair.of(false, "Disconnect failed");
        }
    }

    private Pair<Long, Long> getUserIdAscending(UserId userAId, UserId userBId) {
        return Pair.of(Math.min(userAId.id(), userBId.id()),
                Math.max(userAId.id(), userBId.id()));
    }
}
