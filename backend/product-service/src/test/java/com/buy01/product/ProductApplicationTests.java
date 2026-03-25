package com.buy01.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testHealthCheck() {
		webTestClient.get().uri("/actuator/health")
				.exchange()
				.expectStatus().isOk();
	}
}
