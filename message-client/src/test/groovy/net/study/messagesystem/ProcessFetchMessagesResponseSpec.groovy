package net.study.messagesystem

import net.study.messagesystem.dto.channel.ChannelId
import net.study.messagesystem.dto.message.Message
import net.study.messagesystem.dto.message.MessageSeqId
import net.study.messagesystem.dto.user.User
import net.study.messagesystem.dto.user.UserId
import net.study.messagesystem.dto.websocket.inbound.FetchMessagesResponse
import net.study.messagesystem.dto.websocket.inbound.MessageNotification
import net.study.messagesystem.dto.websocket.outbound.ReadMessageAck
import net.study.messagesystem.service.MessageService
import net.study.messagesystem.service.TerminalService
import net.study.messagesystem.service.UserService
import net.study.messagesystem.service.WebSocketService
import spock.lang.Specification

class ProcessFetchMessagesResponseSpec extends Specification{

    private UserService userService;
    private MessageService messageService;
    private WebSocketService webSocketService = Mock()
    private TerminalService terminalService = Mock()

    def setup() {
        userService = new UserService()
        messageService = new MessageService(userService, terminalService)
        messageService.setWebSocketService(webSocketService)
    }

    def "사용자가 입장한 채널과 다른 채널의 메세지를 받으면 무시한다."() {
        given:
        userService.moveToChannel(new ChannelId(5))

        when:
        messageService.receiveMessage(new MessageNotification(new ChannelId(6), new MessageSeqId(0), "Bob", "0 hi!"))

        then:
        1 * terminalService.printSystemMessage("Invalid channel id. ignore message")
    }

    def "누락된 메세지 응답을 받았을 때 응답이 마지막으로 읽은 메세지 바로 다음 메세지라면 응답을 버퍼에 넣고 버퍼를 처리한다."() {
        given:
        def user = new User(new UserId(1), "Bob")
        def channelId = new ChannelId(5)
        def fetchMessages = List.of(
                new Message(channelId, new MessageSeqId(9), user.username(), "9 girl"),
                new Message(channelId, new MessageSeqId(8), user.username(), "8 bad"))

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(new MessageSeqId(7))
        userService.addMessage(new Message(channelId, new MessageSeqId(11), user.username(), "11 hi"))
        userService.addMessage(new Message(channelId, new MessageSeqId(10), user.username(), "10 hello"))
        userService.addMessage(new Message(channelId, new MessageSeqId(13), user.username(), "13 good"))

        when:
        messageService.receiveMessage(new FetchMessagesResponse(channelId, fetchMessages))

        then:
        1 * terminalService.printMessage(_, "8 bad")
        1 * terminalService.printMessage(_, "9 girl")
        1 * terminalService.printMessage(_, "10 hello")
        1 * terminalService.printMessage(_, "11 hi")
        0 * terminalService.printMessage(_, "13 good")

        and:
        userService.getLastReadMessageSeqId().id() == 11
        userService.getBufferSize() == 1
    }

    def "누락된 메세지 응답을 받았을 때 응답이 저장된 메세지와 중복이라면 무시한다."() {
        given:
        def user = new User(new UserId(1), "Bob")
        def channelId = new ChannelId(5)
        def fetchMessages = List.of(
                new Message(channelId, new MessageSeqId(10), user.username(), "10 girl"),
                new Message(channelId, new MessageSeqId(11), user.username(), "11 bad"))

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(new MessageSeqId(9))
        userService.addMessage(new Message(channelId, new MessageSeqId(11), user.username(), "11 hi"))
        userService.addMessage(new Message(channelId, new MessageSeqId(10), user.username(), "10 hello"))
        userService.addMessage(new Message(channelId, new MessageSeqId(13), user.username(), "13 good"))

        when:
        messageService.receiveMessage(new FetchMessagesResponse(channelId, fetchMessages))

        then:
        1 * terminalService.printMessage(_, "10 hello")
        1 * terminalService.printMessage(_, "11 hi")
        0 * terminalService.printMessage(_, "13 good")

        and:
        userService.getBufferSize() == 1
        userService.getLastReadMessageSeqId().id() == 11
    }

    def "버퍼에 저장된 메세지가 LastReadMessageSeqId 보다 작거나 같으면 버린다."() {
        given:
        def user = new User(new UserId(1), "Bob")
        def channelId = new ChannelId(5)
        def fetchMessages = List.of(
                new Message(channelId, new MessageSeqId(10), user.username(), "10 girl"),
                new Message(channelId, new MessageSeqId(11), user.username(), "11 bad"))

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(new MessageSeqId(13))
        userService.addMessage(new Message(channelId, new MessageSeqId(9), user.username(), "9 hi"))
        userService.addMessage(new Message(channelId, new MessageSeqId(8), user.username(), "8 hello"))
        userService.addMessage(new Message(channelId, new MessageSeqId(13), user.username(), "13 good"))

        when:
        messageService.receiveMessage(new FetchMessagesResponse(channelId, fetchMessages))

        then:
        0 * terminalService.printMessage(_, _)

        and:
        userService.isBufferEmpty()
        userService.getLastReadMessageSeqId().id() == 13
    }

    def "누락 메세지를 처리했으면 Ack를 보내야 한다."() {
        given:
        def user = new User(new UserId(1), "Bob")
        def channelId = new ChannelId(5)
        def fetchMessages = List.of(
                new Message(channelId, new MessageSeqId(9), user.username(), "9 girl"),
                new Message(channelId, new MessageSeqId(8), user.username(), "8 bad"))

        def expectReadMessageAckRequest = [
                new ReadMessageAck(channelId, new MessageSeqId(8)),
                new ReadMessageAck(channelId, new MessageSeqId(9))
        ]

        userService.moveToChannel(channelId)
        userService.setLastReadMessageSeqId(new MessageSeqId(7))

        when:
        messageService.receiveMessage(new FetchMessagesResponse(channelId, fetchMessages))

        then:
        1 * webSocketService.sendMessage(expectReadMessageAckRequest[0])
        1 * webSocketService.sendMessage(expectReadMessageAckRequest[1])

        and:
        userService.isBufferEmpty()
        userService.getLastReadMessageSeqId().id() == 9
    }
}
