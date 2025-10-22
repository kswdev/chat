package net.study.messagesystem.dto

import com.fasterxml.jackson.databind.ObjectMapper
import net.study.messagesystem.constant.UserConnectionStatus
import net.study.messagesystem.domain.channel.ChannelId
import net.study.messagesystem.dto.websocket.inbound.AcceptRequest
import net.study.messagesystem.dto.websocket.inbound.BaseRequest
import net.study.messagesystem.dto.websocket.inbound.CreateRequest
import net.study.messagesystem.dto.websocket.inbound.DisconnectRequest
import net.study.messagesystem.dto.websocket.inbound.EnterRequest
import net.study.messagesystem.dto.websocket.inbound.FetchChannelInviteCodeRequest
import net.study.messagesystem.dto.websocket.inbound.FetchUserConnectionsRequest
import net.study.messagesystem.dto.websocket.inbound.FetchUserInviteCodeRequest
import net.study.messagesystem.dto.websocket.inbound.InviteRequest
import net.study.messagesystem.dto.websocket.inbound.KeepAliveRequest
import net.study.messagesystem.dto.websocket.inbound.RejectRequest
import net.study.messagesystem.dto.websocket.inbound.WriteMessage
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
        payload                                                                                    | expectedClass                 | validate
        '{"type" : "KEEP_ALIVE"}'                                                                  | KeepAliveRequest              | {req -> (req as KeepAliveRequest).getType() == 'KEEP_ALIVE'}
        '{"type" : "ENTER_REQUEST", "channelId": "1"}'                                             | EnterRequest                  | {req -> (req as EnterRequest).getChannelId() == new ChannelId(1L)}
        '{"type" : "DISCONNECT_REQUEST", "username": "username"}'                                  | DisconnectRequest             | {req -> (req as DisconnectRequest).getUsername() == 'username'}
        '{"type" : "ACCEPT_REQUEST", "username": "inviterUsername"}'                               | AcceptRequest                 | {req -> (req as AcceptRequest).getUsername() == 'inviterUsername'}
        '{"type" : "REJECT_REQUEST", "username": "inviterUsername"}'                               | RejectRequest                 | {req -> (req as RejectRequest).getUsername() == 'inviterUsername'}
        '{"type" : "INVITE_REQUEST", "userInviteCode" : "testInviteCode123"}'                      | InviteRequest                 | {req -> (req as InviteRequest).userInviteCode.code() == 'testInviteCode123'}
        '{"type" : "WRITE_MESSAGE", "username" : "sender", "content" : "테스트 내용"}'                | WriteMessage | { req -> (req as WriteMessage).getUsername() == 'sender' && (req as WriteMessage).getContent() == '테스트 내용'}
        '{"type" : "CREATE_REQUEST", "title": "채널 생성", "participantUsernames": ["kim", "hong"]}' | CreateRequest                 | {req -> (req as CreateRequest).getTitle() == "채널 생성" && (req as CreateRequest).getParticipantUsernames().size() == 2}
        '{"type" : "FETCH_USER_INVITE_CODE_REQUEST"}'                                              | FetchUserInviteCodeRequest    | {req -> (req as FetchUserInviteCodeRequest).getType() == 'FETCH_USER_INVITE_CODE_REQUEST'}
        '{"type" : "FETCH_USER_CONNECTIONS_REQUEST", "status": "PENDING"}'                         | FetchUserConnectionsRequest   | {req -> (req as FetchUserConnectionsRequest).getStatus() == UserConnectionStatus.PENDING}
        '{"type" : "FETCH_CHANNEL_INVITE_CODE_REQUEST", "channelId": "1"}'                         | FetchChannelInviteCodeRequest | {req -> (req as FetchChannelInviteCodeRequest).getChannelId() == new ChannelId(1L)}
    }
}
