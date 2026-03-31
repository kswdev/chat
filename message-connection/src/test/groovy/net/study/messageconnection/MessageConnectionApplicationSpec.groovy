package net.study.messageconnection


import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@SpringBootTest(classes = MessageConnectionApplication)
@TestPropertySource(properties = "server.id=test")
class MessageConnectionApplicationSpec extends Specification{

    void contextLoads() {
        expect:
        true
    }
}
