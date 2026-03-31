package net.study.messageconnection.dto

import com.fasterxml.jackson.databind.ObjectMapper
import net.study.messageconnection.constant.UserConnectionStatus
import net.study.messageconnection.domain.channel.ChannelId
import net.study.messageconnection.dto.websocket.inbound.*
import net.study.messageconnection.util.JsonUtil
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
        '{"type" : "KEEP_ALIVE"}'                                                                  | KeepAliveRequest              | { req -> (req as KeepAliveRequest).getType() == 'KEEP_ALIVE'}
        '{"type" : "ENTER_REQUEST", "channelId": "1"}'                                             | EnterRequest                  | { req -> (req as EnterRequest).getChannelId() == new ChannelId(1L)}
        '{"type" : "DISCONNECT_REQUEST", "username": "username"}'                                  | DisconnectRequest             | { req -> (req as DisconnectRequest).getUsername() == 'username'}
        '{"type" : "ACCEPT_REQUEST", "username": "inviterUsername"}'                               | AcceptRequest                 | { req -> (req as AcceptRequest).getUsername() == 'inviterUsername'}
        '{"type" : "REJECT_REQUEST", "username": "inviterUsername"}'                               | RejectRequest                 | { req -> (req as RejectRequest).getUsername() == 'inviterUsername'}
        '{"type" : "INVITE_REQUEST", "userInviteCode" : "testInviteCode123"}'                      | InviteRequest                 | { req -> (req as InviteRequest).getUserInviteCode().code() == 'testInviteCode123'}
        '{"type" : "WRITE_MESSAGE", "channelId" : "1", "content" : "테스트 내용", "serial" : "1"}'    | WriteMessage                    | { req -> (req as WriteMessage).getChannelId() == new ChannelId(1L) && (req as WriteMessage).getContent() == '테스트 내용'}
        '{"type" : "CREATE_REQUEST", "title": "채널 생성", "participantUsernames": ["kim", "hong"]}' | CreateRequest                   | { req -> (req as CreateRequest).getTitle() == "채널 생성" && (req as CreateRequest).getParticipantUsernames().size() == 2}
        '{"type" : "FETCH_USER_INVITE_CODE_REQUEST"}'                                              | FetchUserInviteCodeRequest    | { req -> (req as FetchUserInviteCodeRequest).getType() == 'FETCH_USER_INVITE_CODE_REQUEST'}
        '{"type" : "FETCH_USER_CONNECTIONS_REQUEST", "status": "PENDING"}'                         | FetchUserConnectionsRequest   | { req -> (req as FetchUserConnectionsRequest).getStatus() == UserConnectionStatus.PENDING}
        '{"type" : "FETCH_CHANNEL_INVITE_CODE_REQUEST", "channelId": "1"}'                         | FetchChannelInviteCodeRequest | { req -> (req as FetchChannelInviteCodeRequest).getChannelId() == new ChannelId(1L)}
    }
}
