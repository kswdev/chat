package net.study.messagesystem

import net.study.messagesystem.dto.channel.ChannelId
import net.study.messagesystem.dto.message.MessageSeqId
import net.study.messagesystem.dto.websocket.inbound.MessageNotification
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
}
