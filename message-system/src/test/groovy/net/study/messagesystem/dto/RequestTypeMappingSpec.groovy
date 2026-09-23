package net.study.messagesystem.dto

import com.fasterxml.jackson.databind.ObjectMapper
import net.study.messagesystem.domain.channel.ChannelId
import net.study.messagesystem.dto.kafka.RecordInterface
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
        payload                                                                                  | expectedClass      | validate
        '{"type" : "WRITE_MESSAGE", "channelId" : "1", "content" : "테스트 내용", "serial" : "1"}'  | WriteMessageRecord | { req -> (req as WriteMessageRecord).channelId() == new ChannelId(1L) && (req as WriteMessageRecord).content() == '테스트 내용' }
    }
}
