package net.study.messageauth

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = MessageAuthApplicationSpec)
class MessageAuthApplicationSpec extends Specification {

    void contextLoads() {
        expect:
        true
    }
}
