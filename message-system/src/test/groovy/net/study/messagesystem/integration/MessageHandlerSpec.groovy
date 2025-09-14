package net.study.messagesystem.integration

import com.fasterxml.jackson.databind.ObjectMapper
import net.study.messagesystem.MessageSystemApplication
import net.study.messagesystem.dto.domain.channel.ChannelId
import net.study.messagesystem.dto.websocket.inbound.WriteMessageRequest
import net.study.messagesystem.service.ChannelService
import net.study.messagesystem.service.UserService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import spock.lang.Specification

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(
        classes = MessageSystemApplication,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class MessageHandlerSpec extends Specification {

    @LocalServerPort
    int port

    @Autowired private UserService userService;
    @Autowired private ObjectMapper objectMapper;
    @SpringBean private ChannelService channelService = Stub();

    def "Group Chat Basic Test"() {
        given:
        register("testUserA", "testPassA")
        register("testUserB", "testPassB")
        register("testUserC", "testPassC")

        channelService.getOnlineParticipantsUserIds(_ as ChannelId) >> List.of(
                userService.getUserId("testUserA").get(),
                userService.getUserId("testUserB").get(),
                userService.getUserId("testUserC").get()
        )

        def sessionIdA = login("testUserA", "testPassA")
        def sessionIdB = login("testUserB", "testPassB")
        def sessionIdC = login("testUserC", "testPassC")
        def (clientA, clientB, clientC) = [createClient(sessionIdA), createClient(sessionIdB), createClient(sessionIdC)]

        when:
        clientA.session.sendMessage(new TextMessage(objectMapper.writeValueAsString(new WriteMessageRequest(new ChannelId(1L), "testUserA", "안녕하세요 testUserA 입니다."))))
        clientB.session.sendMessage(new TextMessage(objectMapper.writeValueAsString(new WriteMessageRequest(new ChannelId(1L), "testUserB", "안녕하세요 testUserB 입니다."))))
        clientC.session.sendMessage(new TextMessage(objectMapper.writeValueAsString(new WriteMessageRequest(new ChannelId(1L), "testUserC", "안녕하세요 testUserC 입니다."))))

        then:
        def resultA = clientA.queue.poll(1, TimeUnit.SECONDS) + clientA.queue.poll(1, TimeUnit.SECONDS)
        def resultB = clientB.queue.poll(1, TimeUnit.SECONDS) + clientB.queue.poll(1, TimeUnit.SECONDS)
        def resultC = clientC.queue.poll(1, TimeUnit.SECONDS) + clientC.queue.poll(1, TimeUnit.SECONDS)

        resultA.contains("testUserB") && resultA.contains("testUserC")
        resultB.contains("testUserA") && resultB.contains("testUserC")
        resultC.contains("testUserA") && resultC.contains("testUserB")

        and:
        clientA.queue.isEmpty()
        clientB.queue.isEmpty()
        clientC.queue.isEmpty()

        cleanup:
        unRegister(sessionIdA)
        unRegister(sessionIdB)
        unRegister(sessionIdC)

        clientA.session?.close()
        clientB.session?.close()
        clientC.session?.close()
    }

    def register(String username, String password) {
        def url = "http://localhost:${port}/api/v1/auth/register"
        def headers = new HttpHeaders(["Content-type": "application/json"])
        def requestBody = [username: username, password: password]
        def jsonBody = objectMapper.writeValueAsString(requestBody)
        def httpEntity = new HttpEntity(jsonBody, headers)
        try {
            new RestTemplate().exchange(url, HttpMethod.POST, httpEntity, String)
        } catch (Exception ignore) {}
    }

    def unRegister(String sessionId) {
        def url = "http://localhost:${port}/api/v1/auth/unregister"
        def headers = new HttpHeaders()
        headers.add("Content-type", "application/json")
        headers.add("Cookie", "SESSION=${sessionId}")
        def httpEntity = new HttpEntity(headers)
        def responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, httpEntity, String)
        responseEntity.body
    }

    def login(String username, String password) {
        def url = "http://localhost:${port}/api/v1/auth/login"
        def headers = new HttpHeaders(["Content-type": "application/json"])
        def requestBody = [username: username, password: password]
        def jsonBody = objectMapper.writeValueAsString(requestBody)
        def httpEntity = new HttpEntity(jsonBody, headers)
        def responseEntity = new RestTemplate().exchange(url, HttpMethod.POST, httpEntity, String)
        def sessionId = responseEntity.body
        sessionId
    }

    def createClient(String sessionId) {
        def url = "ws://localhost:${port}/ws/v1/message"
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(5)
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders()
        headers.add("Cookie", "SESSION=${sessionId}")
        def client = new StandardWebSocketClient();
        def webSocketSession = client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                blockingQueue.put(message.payload)
            }
        },headers, new URI(url)).get()

        return [queue: blockingQueue, session: webSocketSession]
    }
}
