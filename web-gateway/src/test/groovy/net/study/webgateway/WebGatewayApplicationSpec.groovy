package net.study.webgateway


import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = WebGatewayApplication)
class WebGatewayApplicationSpec extends Specification {

	void contextLoads() {
		expect:
		true
	}
}
