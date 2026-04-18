package net.study.messageconnectionflux.adpater.out.kafka

import net.study.messageconnectionflux.adpter.out.kafka.KafkaProducer
import net.study.messageconnectionflux.application.dto.kafka.RecordInterface
import net.study.messageconnectionflux.application.port.out.EventProducer
import net.study.messageconnectionflux.util.JsonUtil
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import spock.lang.Specification

class KafkaProducerSpec extends Specification {

    KafkaSender<String, String> kafkaSender = Mock(KafkaSender)
    JsonUtil jsonUtil = Mock(JsonUtil)
    RecordInterface record = Mock(RecordInterface)
    Runnable callback = Mock(Runnable)

    EventProducer producer = new KafkaProducer(kafkaSender, jsonUtil)

    def setup() {
        producer.requestTopic = "test-topic"
    }

    def "sendRequest - 성공 시 callback 실행 안함"() {
        given:
        def json = '{"test":"value"}'
        def senderResult = Stub(SenderResult) {
            exception() >> null
        }

        when:
        producer.sendRequest(record, callback).block()

        then:
        1 * jsonUtil.toJson(record) >> Optional.of(json)
        1 * kafkaSender.send(_ as Mono<SenderRecord<String, String, Void>>) >> Flux.just(senderResult)
        0 * callback.run()
    }

    def "sendRequest - 실패 시 callback 실행"() {
        given:
        def json = '{"test":"value"}'
        def senderResult = Stub(SenderResult) {
            exception() >> new RuntimeException("fail")
        }

        when:
        producer.sendRequest(record, callback).block()

        then:
        1 * jsonUtil.toJson(record) >> Optional.of(json)
        1 * kafkaSender.send(_ as Mono<SenderRecord<String, String, Void>>) >> Flux.just(senderResult)
        1 * callback.run()
    }
}
