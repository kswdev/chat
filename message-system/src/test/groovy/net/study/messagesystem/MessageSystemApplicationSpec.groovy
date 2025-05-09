package net.study.messagesystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification;

@SpringBootTest(classes = MessageSystemApplication)
class MessageSystemApplicationSpec extends Specification{

    void contextLoads() {
        expect:
        true
    }
}
