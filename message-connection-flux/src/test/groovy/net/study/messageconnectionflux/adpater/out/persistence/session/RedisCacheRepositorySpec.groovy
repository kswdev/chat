package net.study.messageconnectionflux.adpater.out.persistence.session

import net.study.messageconnectionflux.adpter.out.persistence.redis.RedisCacheRepository
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.data.redis.core.ReactiveValueOperations
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class RedisCacheRepositorySpec extends Specification {

    private ReactiveRedisOperations redisOperations = Stub(ReactiveRedisOperations)
    private ReactiveValueOperations valueOperations = Stub(ReactiveValueOperations)

    private RedisCacheRepository cacheService

    def setup() {
        cacheService = new RedisCacheRepository(redisOperations)
        redisOperations.opsForValue() >> valueOperations
    }

    def "redis에서 키가 있는 단일 값 조회"() {
        given:
        valueOperations.get("key") >> Mono.just("value")

        when:
        def mono = cacheService.get("key")

        then:
        StepVerifier.create(mono)
                .expectNext("value")
                .verifyComplete()
    }

    def "redis에서 키가 없는 빈 단일 값 조회"() {
        given:
        valueOperations.get("key") >> Mono.empty()

        when:
        def mono = cacheService.get("key")

        then:
        StepVerifier.create(mono)
                .verifyComplete()
    }

    def "redis에서 여러 키로 Collection 값 조회"() {
        given:
        def keys = ["key1", "key2"]
        def values = ["value1", "value2"]

        valueOperations.multiGet(keys) >> Mono.just(values)

        when:
        def mono = cacheService.get(keys)

        then:
        StepVerifier.create(mono)
                .expectNext(values)
                .verifyComplete()
    }
}
