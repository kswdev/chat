package net.study.messagesystem.dto

import com.fasterxml.jackson.databind.ObjectMapper
import net.study.messagesystem.constant.UserConnectionStatus
import net.study.messagesystem.domain.channel.ChannelId
import net.study.messagesystem.dto.kafka.AcceptRequestRecord
import net.study.messagesystem.dto.kafka.CreateRequestRecord
import net.study.messagesystem.dto.kafka.DisconnectRequestRecord
import net.study.messagesystem.dto.kafka.EnterRequestRecord
import net.study.messagesystem.dto.kafka.FetchChannelInviteCodeRequestRecord
import net.study.messagesystem.dto.kafka.FetchUserConnectionsRequestRecord
import net.study.messagesystem.dto.kafka.FetchUserInviteCodeRequestRecord
import net.study.messagesystem.dto.kafka.InviteRequestRecord
import net.study.messagesystem.dto.kafka.RecordInterface
import net.study.messagesystem.dto.kafka.RejectRequestRecord
import net.study.messagesystem.dto.kafka.WriteMessageRecord
import net.study.messagesystem.util.JsonUtil
import spock.lang.Specification

class RequestTypeMappingSpec extends Specification {

    JsonUtil jsonUtil = new JsonUtil(new ObjectMapper());

    def "DTO 형식의 JSON 문자열을 해당 타입의 DTO로 변환할 수 있다."() {
        given:
        String jsonBody = payload

        when:
        RecordInterface recordInterface = jsonUtil.fromJson(jsonBody, RecordInterface).get()

        then:
        recordInterface.getClass() == expectedClass
        validate(recordInterface)

        where:
        payload                                                                                  | expectedClass                       | validate
        '{"type" : "ENTER_REQUEST", "channelId": "1"}'                                           | EnterRequestRecord                  | { req -> (req as EnterRequestRecord).channelId() == new ChannelId(1L) }
        '{"type" : "DISCONNECT_REQUEST", "username": "username"}'                                | DisconnectRequestRecord             | { req -> (req as DisconnectRequestRecord).username() == 'username' }
        '{"type" : "ACCEPT_REQUEST", "username": "inviterUsername"}'                             | AcceptRequestRecord                 | { req -> (req as AcceptRequestRecord).username() == 'inviterUsername' }
        '{"type" : "REJECT_REQUEST", "username": "inviterUsername"}'                             | RejectRequestRecord                 | { req -> (req as RejectRequestRecord).username() == 'inviterUsername' }
        '{"type" : "INVITE_REQUEST", "userInviteCode" : "testInviteCode123"}'                    | InviteRequestRecord                 | { req -> (req as InviteRequestRecord).userInviteCode().code() == 'testInviteCode123' }
        '{"type" : "WRITE_MESSAGE", "channelId" : "1", "content" : "테스트 내용", "serial" : "1"}'  | WriteMessageRecord                | { req -> (req as WriteMessageRecord).channelId() == new ChannelId(1L) && (req as WriteMessageRecord).content() == '테스트 내용' }
        '{"type" : "CREATE_REQUEST", "title": "채널 생성", "participantUsernames": ["kim", "hong"]}'| CreateRequestRecord               | { req -> (req as CreateRequestRecord).title() == "채널 생성" && (req as CreateRequestRecord).participantUsernames().size() == 2 }
        '{"type" : "FETCH_USER_INVITE_CODE_REQUEST"}'                                            | FetchUserInviteCodeRequestRecord    | { req -> (req as FetchUserInviteCodeRequestRecord).type() == 'FETCH_USER_INVITE_CODE_REQUEST' }
        '{"type" : "FETCH_USER_CONNECTIONS_REQUEST", "status": "PENDING"}'                       | FetchUserConnectionsRequestRecord   | { req -> (req as FetchUserConnectionsRequestRecord).status() == UserConnectionStatus.PENDING }
        '{"type" : "FETCH_CHANNEL_INVITE_CODE_REQUEST", "channelId": "1"}'                       | FetchChannelInviteCodeRequestRecord | { req -> (req as FetchChannelInviteCodeRequestRecord).channelId() == new ChannelId(1L) }
    }
}
