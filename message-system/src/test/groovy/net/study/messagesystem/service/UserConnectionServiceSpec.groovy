package net.study.messagesystem.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.study.messagesystem.constant.UserConnectionStatus
import net.study.messagesystem.dto.projection.InviterUserIdProjection
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection
import net.study.messagesystem.domain.user.InviteCode
import net.study.messagesystem.domain.user.User
import net.study.messagesystem.domain.user.UserId
import net.study.messagesystem.entity.user.UserEntity
import net.study.messagesystem.entity.user.connection.UserConnectionEntity
import net.study.messagesystem.repository.connection.UserConnectionRepository
import net.study.messagesystem.util.JsonUtil
import org.springframework.data.util.Pair
import spock.lang.Specification

class UserConnectionServiceSpec extends Specification {

    private final UserService userService = Stub()
    private final CacheService cacheService = Stub()
    private final UserConnectionRepository userConnectionRepository = Stub()

    private final UserConnectionLimitService userConnectionLimitService = new UserConnectionLimitService(cacheService, userConnectionRepository)
    private final UserConnectionService userConnectionService = new UserConnectionService(new JsonUtil(new ObjectMapper()), userService, cacheService, userConnectionLimitService, userConnectionRepository)

    def "사용자 연결 신청에 대한 테스트"() {
        given:
        userService.getUserIdName(inviteCodeOfTargetUser) >> Optional.of(new User(targetUserId, targetUsername))
        userService.getUsername(senderUserId) >> Optional.of(senderUsername)
        userService.getUserReference(senderUserId) >> UserEntity.testUser(senderUserId.id())
        userService.getUserReference(targetUserId) >> UserEntity.testUser(targetUserId.id())
        userConnectionRepository.findUserConnectionStatusByPartnerAUser_userIdAndPartnerBUser_userId(_ as Long, _ as Long) >> {
            Optional.of(Stub(UserConnectionStatusProjection) {
                getStatus() >> beforeConnectionStatus.name()
            })
        }

        when:
        Pair<Optional<UserId>, String> result = userConnectionService.invite(senderUserId, usedInviteCode)

        then:
        result == expectedResult

        where:
        scenario              | senderUserId  | senderUsername | targetUserId  | targetUsername | inviteCodeOfTargetUser      | usedInviteCode              | beforeConnectionStatus            | expectedResult
        'Valid invite code'   | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.NONE         | Pair.of(Optional.of(new UserId(2)), 'userA')
        'Valid invite code'   | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.DISCONNECTED | Pair.of(Optional.of(new UserId(2)), 'userA')
        'Already connected'   | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.ACCEPTED     | Pair.of(Optional.of(new UserId(2)), "Already connected with " + targetUsername)
        'Already Invited'     | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.PENDING      | Pair.of(Optional.of(new UserId(2)), "Already Invited to " + targetUsername);
        'Reject Invited'      | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.REJECTED     | Pair.of(Optional.of(new UserId(2)), "Already Invited to " + targetUsername);
        'Invalid invite code' | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('nobody')    | new InviteCode('user2Code') | UserConnectionStatus.NONE         | Pair.of(Optional.empty(), "Invite failed.");
        'Self Invite'         | new UserId(1) | 'userA'        | new UserId(1) | 'userB'        | new InviteCode('user1Code') | new InviteCode('user1Code') | UserConnectionStatus.NONE         | Pair.of(Optional.empty(), "Invite failed.");
    }

    def "사용자 연결 신청에 대한 요청 수락 테스트"() {
        given:
        userService.getUserId(targetUsername) >> Optional.of(targetUserId)
        userService.getUsername(senderUserId) >> Optional.of(senderUsername)

        userConnectionRepository.findInviterUserIdByPartnerAUser_userIdAndPartnerBUser_userId(
                Math.min(targetUserId.id(), senderUserId.id()),
                Math.max(targetUserId.id(), senderUserId.id())
        ) >> Optional.of(Stub(InviterUserIdProjection) {
            getInviterUserId() >> inviterUserId.id()
        })

        userConnectionRepository.findUserConnectionStatusByPartnerAUser_userIdAndPartnerBUser_userId(
                Math.min(targetUserId.id(), senderUserId.id()),
                Math.max(targetUserId.id(), senderUserId.id())
        ) >> Optional.of(Stub(UserConnectionStatusProjection) {
            getStatus() >> connectionStatus
        })

        userConnectionRepository.findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(
                Math.min(targetUserId.id(), senderUserId.id()),
                Math.max(targetUserId.id(), senderUserId.id()),
                connectionStatus
        ) >> {
            UserEntity inviter = UserEntity.testUser(targetUserId.id())
            UserEntity accepter = UserEntity.testUser(senderUserId.id())

            if (inviter.getUserId() == 5L || inviter.getUserId() == 7L)
                inviter.setConnectionCount(1_000)
            if (accepter.getUserId() == 5L || accepter.getUserId() == 7L)
                accepter.setConnectionCount(1_000)

            return Optional.of(UserConnectionEntity.testConnection(
                    inviter,
                    accepter,
                    connectionStatus,
                    targetUserId.id()
            ))
        }

        when:
        Pair<Optional<UserId>, String> result = userConnectionService.accept(senderUserId, targetUsername)

        then:
        result == expectedResult

        where:
        scenario            | senderUserId   | senderUsername   | targetUserId  | targetUsername | inviterUserId | connectionStatus              | expectedResult
        'Valid accept'      | new UserId(2)  | 'userB'          | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.PENDING  | Pair.of(Optional.of(targetUserId), senderUsername)
        'Already connected' | new UserId(2)  | 'userB'          | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.ACCEPTED | Pair.of(Optional.empty(), 'Invalid status or userId.');
        'Self accept'       | new UserId(1)  | 'userA'          | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.PENDING  | Pair.of(Optional.empty(), 'accept failed.')
        'Invalid invite'    | new UserId(2)  | 'userB'          | new UserId(1) | 'userA'        | new UserId(3) | UserConnectionStatus.PENDING  | Pair.of(Optional.empty(), 'accept failed.')
        'Already rejected'  | new UserId(2)  | 'userB'          | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.REJECTED | Pair.of(Optional.empty(), 'Invalid status or userId.');
        'Limit by self'     | new UserId(5)  | 'userE'          | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.PENDING  | Pair.of(Optional.empty(), 'Connection limit reached')
        'Limit by other'    | new UserId(2)  | 'userB'          | new UserId(7) | 'userG'        | new UserId(7) | UserConnectionStatus.PENDING  | Pair.of(Optional.empty(), 'Connection limit reached by other user')
    }

    def "사용자 연결 신청 거절에 대한 테스트"() {
        given:
        userService.getUserId(targetUsername) >> Optional.of(targetUserId)

        userConnectionRepository.findInviterUserIdByPartnerAUser_userIdAndPartnerBUser_userId(
                Math.min(senderUserId.id(), targetUserId.id()),
                Math.max(senderUserId.id(), targetUserId.id())
        ) >> Optional.of(Stub(InviterUserIdProjection) {
            getInviterUserId() >> inviterUserId.id()
        })

        userConnectionRepository.findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(
                Math.min(senderUserId.id(), targetUserId.id()),
                Math.max(senderUserId.id(), targetUserId.id()),
                UserConnectionStatus.PENDING
        ) >> {
            UserEntity userA = UserEntity.testUser(senderUserId.id())
            UserEntity userB = UserEntity.testUser(targetUserId.id())

            return Optional.of(UserConnectionEntity.create(userB, userA, targetUserId.id()))
        }

        when:
        def result = userConnectionService.reject(senderUserId, targetUsername)

        then:
        result == expectResult

        where:
        scenario           | senderUserId  | senderUsername | targetUserId  | targetUsername | inviterUserId | connectionStatus              | expectResult
        'Valid reject'     | new UserId(2) | 'userB'        | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.PENDING  | Pair.of(true, targetUsername)
        'Same user reject' | new UserId(2) | 'userB'        | new UserId(2) | 'userB'        | new UserId(2) | UserConnectionStatus.PENDING  | Pair.of(false, "Reject failed")
        'Invalid reject'   | new UserId(2) | 'userB'        | new UserId(1) | 'userA'        | new UserId(3) | UserConnectionStatus.PENDING  | Pair.of(false, "Reject failed")
    }

    def "사용자 연결 끊기 요청에 대한 테스트" () {
        given:
        userService.getUserId(targetUsername) >> Optional.of(targetUserId)
        userConnectionRepository.findInviterUserIdByPartnerAUser_userIdAndPartnerBUser_userId(
                Math.min(senderUserId.id(), targetUserId.id()),
                Math.max(senderUserId.id(), targetUserId.id())
        ) >> Optional.of(Stub(InviterUserIdProjection) {
            getInviterUserId() >> inviterUserId.id()
        })

        userConnectionRepository.findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(
                Math.min(senderUserId.id(), targetUserId.id()),
                Math.max(senderUserId.id(), targetUserId.id()),
                UserConnectionStatus.ACCEPTED
        ) >> {
            UserEntity userA = UserEntity.testUser(senderUserId.id())
            UserEntity userB = UserEntity.testUser(targetUserId.id())

            if (userA.getUserId() != 8) {
                userA.setConnectionCount(1)
                userB.setConnectionCount(1)
            }

            return Optional.of(UserConnectionEntity.testConnection(userB, userA, connectionStatus ,inviterUserId.id()))
        }

        when:
        Pair<Boolean, String> result = userConnectionService.disconnect(senderUserId, targetUsername)

        then:
        result == expectResult

        where:
        scenario               | senderUserId  | senderUsername | targetUserId  | targetUsername | inviterUserId | connectionStatus              | expectResult
        'Valid disconnect'     | new UserId(2) | 'userB'        | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.ACCEPTED | Pair.of(true, targetUsername)
        'Valid disconnect'     | new UserId(2) | 'userB'        | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.REJECTED | Pair.of(true, targetUsername)
        'limit reached zero'   | new UserId(8) | 'userB'        | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.ACCEPTED | Pair.of(false, "Disconnect failed")
        'PENDING disconnect'   | new UserId(2) | 'userB'        | new UserId(1) | 'userA'        | new UserId(1) | UserConnectionStatus.PENDING  | Pair.of(false, "Disconnect failed")
        'Same user disconnect' | new UserId(2) | 'userB'        | new UserId(2) | 'userB'        | new UserId(2) | UserConnectionStatus.ACCEPTED | Pair.of(false, "Disconnect failed")
    }
}