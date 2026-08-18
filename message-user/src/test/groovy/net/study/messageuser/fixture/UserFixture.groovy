package net.study.messageuser.fixture

import net.study.messageuser.entity.user.UserEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class UserFixture {

    static List<UserEntity> createUsers(int count) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder()
        (1..count).collect { i -> new UserEntity("user$i", passwordEncoder.encode("user$i"))}
    }
}
