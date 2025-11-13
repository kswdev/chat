package net.study.messageconnection


import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = MessageConnectionApplication)
class MessageConnectionApplicationSpec extends Specification{

    void contextLoads() {
        expect:
        true
    }
}
