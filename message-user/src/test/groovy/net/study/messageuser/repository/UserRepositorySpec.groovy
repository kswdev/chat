package net.study.messageuser.repository

import net.study.messageuser.entity.user.UserEntity
import net.study.messageuser.fixture.UserFixture
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositorySpec extends Specification {

    @Autowired
    UserRepository userRepository

    @Disabled("실제 DB에 유저 1000명을 삽입하는 테스트")
    @Rollback(false)
    def "create 1000 Users"() {
        given:
        List<UserEntity> users = UserFixture.createUsers(1000)

        when:
        userRepository.saveAll(users)

        then:
        userRepository.count() == 1000L
    }
}
