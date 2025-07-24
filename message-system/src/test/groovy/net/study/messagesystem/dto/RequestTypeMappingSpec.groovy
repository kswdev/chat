package net.study.messagesystem.dto

import com.fasterxml.jackson.databind.ObjectMapper
import net.study.messagesystem.dto.websocket.inbound.AcceptRequest
import net.study.messagesystem.dto.websocket.inbound.BaseRequest
import net.study.messagesystem.dto.websocket.inbound.InviteRequest
import net.study.messagesystem.dto.websocket.inbound.KeepAliveRequest
import net.study.messagesystem.dto.websocket.inbound.WriteMessageRequest
import net.study.messagesystem.util.JsonUtil
import spock.lang.Specification

class RequestTypeMappingSpec extends Specification {

    JsonUtil jsonUtil = new JsonUtil(new ObjectMapper());

    def "DTO 형식의 JSON 문자열을 해당 타입의 DTO로 변환할 수 있다."() {
        given:
        String jsonBody = payload

        when:
        BaseRequest request = jsonUtil.fromJson(jsonBody, BaseRequest).get()

        then:
        request.getClass() == expectedClass
        validate(request)

        where:
        payload                                                                     | expectedClass       | validate
        '{"type" : "KEEP_ALIVE"}'                                                   | KeepAliveRequest    | {req -> (req as KeepAliveRequest).getType() == 'KEEP_ALIVE'}
        '{"type" : "ACCEPT_REQUEST", "username": "inviterUsername"}'                | AcceptRequest       | {req -> (req as AcceptRequest).getUsername() == 'inviterUsername'}
        '{"type" : "INVITE_REQUEST", "userInviteCode" : "testInviteCode123"}'       | InviteRequest       | {req -> (req as InviteRequest).userInviteCode.code() == 'testInviteCode123'}
        '{"type" : "WRITE_MESSAGE", "username" : "sender", "content" : "테스트 내용"}' | WriteMessageRequest | {req -> (req as WriteMessageRequest).getUsername() == 'sender' && (req as WriteMessageRequest).getContent() == '테스트 내용'}
    }
}
