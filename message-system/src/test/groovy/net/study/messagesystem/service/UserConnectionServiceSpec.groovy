package net.study.messagesystem.service

import net.study.messagesystem.constant.UserConnectionStatus
import net.study.messagesystem.dto.projection.InviterUserIdProjection
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection
import net.study.messagesystem.dto.user.InviteCode
import net.study.messagesystem.dto.user.User
import net.study.messagesystem.dto.user.UserId
import net.study.messagesystem.entity.user.UserEntity
import net.study.messagesystem.entity.user.connection.UserConnectionEntity
import net.study.messagesystem.repository.UserConnectionRepository
import org.springframework.data.util.Pair
import spock.lang.Specification

class UserConnectionServiceSpec extends Specification {

    private final UserService userService = Stub()
    private final UserConnectionRepository userConnectionRepository = Stub()

    private final UserConnectionService userConnectionService = new UserConnectionService(userService, userConnectionRepository)

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
        userService.getUserId(inviterUsername) >> Optional.of(inviterUserId)
        userService.getUsername(accepterUserId) >> Optional.of(accepterUsername)

        userConnectionRepository.findInviterUserIdByPartnerAUser_userIdAndPartnerBUser_userId(
                Math.min(inviterUserId.id(), accepterUserId.id()),
                Math.max(inviterUserId.id(), accepterUserId.id())
        ) >> Optional.of(Stub(InviterUserIdProjection) {
            getInviterUserId() >> inviterUserId.id()
        })

        userConnectionRepository.findUserConnectionStatusByPartnerAUser_userIdAndPartnerBUser_userId(
                Math.min(inviterUserId.id(), accepterUserId.id()),
                Math.max(inviterUserId.id(), accepterUserId.id())
        ) >> Optional.of(Stub(UserConnectionStatusProjection) {
            getStatus() >> connectionStatus
        })

        userConnectionRepository.findByPartnerAUser_userIdAndPartnerBUser_userIdAndStatus(
                Math.min(inviterUserId.id(), accepterUserId.id()),
                Math.max(inviterUserId.id(), accepterUserId.id()),
                connectionStatus
        ) >> {
            UserEntity inviter = UserEntity.testUser(inviterUserId.id())
            UserEntity accepter = UserEntity.testUser(accepterUserId.id())

            if (inviter.getUserId() == 5L || inviter.getUserId() == 7L)
                inviter.setConnectionCount(1_000)
            if (accepter.getUserId() == 5L || accepter.getUserId() == 7L)
                accepter.setConnectionCount(1_000)

            return Optional.of(UserConnectionEntity.testConnection(
                    inviter,
                    accepter,
                    connectionStatus,
                    inviterUserId.id()
            ))
        }

        when:
        Pair<Optional<UserId>, String> result = userConnectionService.accept(accepterUserId, inviterUsername)

        then:
        result == expectedResult

        where:
        scenario            | accepterUserId | accepterUsername | inviterUserId | inviterUsername | connectionStatus              | expectedResult
        'Valid accept'      | new UserId(2)  | 'userB'          | new UserId(1) | 'userA'         | UserConnectionStatus.PENDING  | Pair.of(Optional.of(inviterUserId), accepterUsername)
        'Already connected' | new UserId(2)  | 'userB'          | new UserId(1) | 'userA'         | UserConnectionStatus.ACCEPTED | Pair.of(Optional.empty(), 'accept failed.')
        'Self accept'       | new UserId(1)  | 'userA'          | new UserId(1) | 'userA'         | UserConnectionStatus.PENDING  | Pair.of(Optional.empty(), 'accept failed.')
        'Invalid invite'    | new UserId(2)  | 'userB'          | new UserId(1) | 'userA'         | UserConnectionStatus.REJECTED | Pair.of(Optional.empty(), 'accept failed.')
        'Invalid invite'    | new UserId(5)  | 'userE'          | new UserId(1) | 'userA'         | UserConnectionStatus.PENDING  | Pair.of(Optional.empty(), 'Connection limit reached')
        'Invalid invite'    | new UserId(2)  | 'userB'          | new UserId(7) | 'userG'         | UserConnectionStatus.PENDING  | Pair.of(Optional.empty(), 'Connection limit reached by other user')
    }
}
