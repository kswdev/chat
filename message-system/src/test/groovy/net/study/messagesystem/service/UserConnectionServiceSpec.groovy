package net.study.messagesystem.service

import net.study.messagesystem.constant.UserConnectionStatus
import net.study.messagesystem.dto.projection.UserConnectionStatusProjection
import net.study.messagesystem.dto.user.InviteCode
import net.study.messagesystem.dto.user.User
import net.study.messagesystem.dto.user.UserId
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

        userConnectionRepository.findByPartnerAUserIdAndPartnerBUserId(_ as Long, _ as Long) >> {
            Optional.of(Stub(UserConnectionStatusProjection) {
                getStatus() >> beforeConnectionStatus.name()
            })
        }

        when:
        Pair<Optional<UserId>, String> result = userConnectionService.invite(senderUserId, usedInviteCode)

        then:
        result == expectedResult

        where:
        scenario            | senderUserId  | senderUsername | targetUserId  | targetUsername | inviteCodeOfTargetUser      | usedInviteCode              | beforeConnectionStatus            | expectedResult
        'Valid invite code' | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.NONE         | Pair.of(Optional.of(new UserId(2)), 'userA')
        'Valid invite code' | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.DISCONNECTED | Pair.of(Optional.of(new UserId(2)), 'userA')
        'Already connected' | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.CONNECTED    | Pair.of(Optional.of(new UserId(2)), "Already connected with " + targetUsername)
        'Already Invited'   | new UserId(1) | 'userA'        | new UserId(2) | 'userB'        | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.PENDING      | Pair.of(Optional.of(new UserId(2)), "Already Invited to " + targetUsername);
        'Reject Invited'    | new UserId(1) | 'userA' | new UserId(2) | 'userB' | new InviteCode('user2Code') | new InviteCode('user2Code') | UserConnectionStatus.REJECTED | Pair.of(Optional.of(new UserId(2)), "Already Invited to " + targetUsername);
        'Invalid invite code' | new UserId(1) | 'userA' | new UserId(2) | 'userB' | new InviteCode('nobody') | new InviteCode('user2Code') | UserConnectionStatus.NONE | Pair.of(Optional.empty(), "User not found");
        'Self Invite'       | new UserId(1) | 'userA' | new UserId(1) | 'userB' | new InviteCode('user1Code') | new InviteCode('user1Code') | UserConnectionStatus.NONE | Pair.of(Optional.empty(), "Cannot invite self");
    }
}
