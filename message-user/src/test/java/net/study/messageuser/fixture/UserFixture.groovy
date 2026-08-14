package net.study.messageuser.fixture

import net.study.messageuser.entity.user.UserEntity

class UserFixture {

    static List<UserEntity> createUsers(int count) {
        (1..count).collect { i -> new UserEntity(username: "user{i}", password: "user{i}")}
    }
}
