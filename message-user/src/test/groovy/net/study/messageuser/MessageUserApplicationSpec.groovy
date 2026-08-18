package net.study.messageuser;

import org.springframework.boot.test.context.SpringBootTest;
import spock.lang.Specification;

@SpringBootTest(classes = MessageUserApplication)
class MessageUserApplicationSpec extends Specification {

    void contextLoads() {
        expect:
        true
    }
}
