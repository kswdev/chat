package net.study.messagesystem

import net.study.messagesystem.dto.channel.ChannelId
import net.study.messagesystem.dto.message.Message
import net.study.messagesystem.dto.message.MessageSeqId
import net.study.messagesystem.dto.user.User
import net.study.messagesystem.dto.user.UserId
import net.study.messagesystem.dto.websocket.inbound.MessageNotification
import net.study.messagesystem.dto.websocket.outbound.FetchMessagesRequest
import net.study.messagesystem.dto.websocket.outbound.ReadMessageAck
import net.study.messagesystem.service.MessageService
import net.study.messagesystem.service.TerminalService
import net.study.messagesystem.service.UserService
import net.study.messagesystem.service.WebSocketService
import spock.lang.Specification;

class ProcessMessageNotificationSpec extends Specification {

    private UserService userService;
    private MessageService messageService;
    private WebSocketService webSocketService = Mock()
    private TerminalService terminalService = Mock()

    def setup() {
        userService = new UserService()
        messageService = new MessageService(userService, terminalService)
        messageService.setWebSocketService(webSocketService)
    }

    def "채널에 입장하지 않은 상태에서 받은 메세지는 무시한다."() {
        given:
        def channelId = new ChannelId(5)
        userService.moveToLobby()

        when:
        messageService.receiveMessage(new MessageNotification(channelId, new MessageSeqId(100), "bob", "hello"))

        then:
        0 * terminalService.printMessage(_, _)
        0 * webSocketService.sendMessage(_)
    }

    def "UserService는 참여중인 채널에서 마지막으로 받은 메세지의 MessageSeqId를 가지고 있다."() {
        given:
        def channelId = new ChannelId(5)
        def messageSeqId = new MessageSeqId(100)
        userService.moveToChannel(channelId)

        when:
        messageService.receiveMessage(new MessageNotification(channelId, new MessageSeqId(100), "bob", "hello"))

        then:
        userService.getLastReadMessageSeqId() == messageSeqId
    }

    def "새로 받은 메세지의 MessageSeqId가 이전에 받은 메세지의 MessageSeqId보다 정확히 1이 크다면 ack를 보낸다."() {
        given:
        def channelId = new ChannelId(5)
        def lastReadMessageSeqId = new MessageSeqId(100)
        def newMessageSeqId = new MessageSeqId(101)
        def expectReadMessageAck = new ReadMessageAck(channelId, newMessageSeqId)

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(lastReadMessageSeqId)

        when:
        messageService.receiveMessage(new MessageNotification(channelId, newMessageSeqId, "bob", "hello"))

        then:
        userService.getLastReadMessageSeqId() == newMessageSeqId

        and:
        1 * webSocketService.sendMessage(expectReadMessageAck)
        1 * terminalService.printMessage(_, _)
    }

    def "새로 받은 메세지 MessageSeqId가 이전에 받은 메세지 MessageSeqId보다 2이상 크다면 누락된 메세지를 요청해야 한다."() {
        given:
        def channelId = new ChannelId(5)
        def lastReadMessageSeqId = new MessageSeqId(100)
        def newMessageSeqId = new MessageSeqId(104)
        def expectFetchMessageRequest = new FetchMessagesRequest(channelId, new MessageSeqId(lastReadMessageSeqId.id()+1), new MessageSeqId(newMessageSeqId.id()-1))

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(lastReadMessageSeqId)

        when:
        messageService.receiveMessage(new MessageNotification(channelId, newMessageSeqId, "bob", "hello"))
        sleep(200)

        then:
        1 * webSocketService.sendMessage(expectFetchMessageRequest)

        and:
        userService.peekMessage().messageSeqId() == newMessageSeqId
    }

    def "새로 받은 메세지 MessageSeqId가 이전에 받은 메세지 MessageSeqId보다 작거나 같으면 무시한다."() {
        given:
        def channelId = new ChannelId(5)
        def lastReadMessageSeqId = new MessageSeqId(100)
        def newMessageSeqId = new MessageSeqId(100)

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(lastReadMessageSeqId)

        when:
        messageService.receiveMessage(new MessageNotification(channelId, newMessageSeqId, "bob", "hello"))

        then:
        0 * webSocketService.sendMessage(_)
        0 * terminalService.printMessage(_, _)
        1 * terminalService.printSystemMessage("Ignore duplicate message id: " + newMessageSeqId);
    }

    def "버퍼에 저장된 메세지가 있으면 처리한다."() {
        given:
        def channelId = new ChannelId(5)
        def user = new User(new UserId(5), "Bob")
        def lastReadMessageSeqId = new MessageSeqId(8)

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(lastReadMessageSeqId)
        userService.addMessage(new Message(channelId, new MessageSeqId(11), user.username(), "11 hi"))
        userService.addMessage(new Message(channelId, new MessageSeqId(10), user.username(), "10 hello"))
        userService.addMessage(new Message(channelId, new MessageSeqId(13), user.username(), "13 good"))

        when:
        messageService.receiveMessage(new MessageNotification(channelId, new MessageSeqId(9), "bob", "9 hello"))

        then:
        1 * terminalService.printMessage(_, "9 hello")
        1 * terminalService.printMessage(_, "10 hello")
        1 * terminalService.printMessage(_, "11 hi")
        0 * terminalService.printMessage(_, "13 good")
    }
}
