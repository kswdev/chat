package net.study.messagesocial.domain.userconnectioncount;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;

class UserConnectionCountTest {

    private static final Long USER_ID = 1L;

    @Test
    void increase_increments_count_when_below_limit() {
        UserConnectionCount count = UserConnectionCount.of(USER_ID, UserConnectionCount.LIMIT - 1);

        count.increase();

        assertThat(count.getCount()).isEqualTo(UserConnectionCount.LIMIT);
    }

    @Test
    void increase_throws_domain_exception_when_already_at_limit() {
        UserConnectionCount count = UserConnectionCount.of(USER_ID, UserConnectionCount.LIMIT);

        Throwable thrown = catchThrowable(count::increase);

        assertThat(thrown).isInstanceOf(LimitExceededException.class);
        assertThat(((LimitExceededException) thrown).getUserId()).isEqualTo(USER_ID);
        assertThat(count.getCount()).isEqualTo(UserConnectionCount.LIMIT);
    }

    @Test
    void decrease_decrements_count_when_above_zero() {
        UserConnectionCount count = UserConnectionCount.of(USER_ID, 1);

        count.decrease();

        assertThat(count.getCount()).isEqualTo(0);
    }

    @Test
    void decrease_does_not_go_below_zero() {
        UserConnectionCount count = UserConnectionCount.of(USER_ID, 0);

        count.decrease();

        assertThat(count.getCount()).isEqualTo(0);
    }
}
