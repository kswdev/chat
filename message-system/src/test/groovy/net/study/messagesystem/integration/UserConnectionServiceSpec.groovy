package net.study.messagesystem.integration

import lombok.RequiredArgsConstructor;
import net.study.messagesystem.MessageSystemApplication
import net.study.messagesystem.dto.domain.user.UserId
import net.study.messagesystem.entity.user.UserEntity
import net.study.messagesystem.repository.UserConnectionRepository
import net.study.messagesystem.repository.UserRepository
import net.study.messagesystem.service.UserConnectionService
import net.study.messagesystem.service.UserService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Specification

import static java.util.Collections.*;

@RequiredArgsConstructor
@SpringBootTest(classes = MessageSystemApplication.class)
class UserConnectionServiceSpec extends Specification {

    @Autowired private UserService userService
    @Autowired private UserConnectionService userConnectionService
    @Autowired private UserRepository userRepository
    @Autowired private UserConnectionRepository userConnectionRepository

    def "연결 요청 수락은 연결 제한 수를 넘길 수 없다."() {
        given:
        (0..19).each{
            def entity = new UserEntity("testUser${it}", "testPass")
            userRepository.save(entity)
        }

        def userIdA = userService.getUserId("testUser0").get()
        def inviteCodeA = userService.getInviteCode(userIdA).get()

        (1..9).each{
            userConnectionService.invite(userService.getUserId("testUser${it}").get(), inviteCodeA)
            userConnectionService.accept(userIdA, "testUser${it}")
        }

        def inviteCodes = (10..19).collect{
            userService.getInviteCode(userService.getUserId("testUser${it}").get()).get()
        }

        inviteCodes.each {userConnectionService.invite(userIdA, it)}

        def results = synchronizedList(new ArrayList<Optional<UserId>>())

        when:
        def threads = (10..19).collect({idx ->
                Thread.start {
                    def userId = userService.getUserId("testUser${idx}").get()
                    results << userConnectionService.accept(userId, "testUser0").getFirst()
                }
        })

        threads*.join()

        then:
        results.count{it.isPresent()} == 1

        cleanup:
        (0..19).each {
            def userId = userService.getUserId("testUser${it}").get()
            userRepository.deleteById(userId.id())
        }
    }
}
