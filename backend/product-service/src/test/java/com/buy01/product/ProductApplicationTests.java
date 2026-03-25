package com.buy01.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

// We use RANDOM_PORT to avoid "Port already in use" errors during Jenkins builds
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void contextLoads() {
		// If this method completes, the Spring Context (and WebFlux) started correctly
	}

	@Test
	void checkHealthEndpoint() {
		// This confirms your API is actually responding
		webTestClient.get().uri("/actuator/health")
				.exchange()
				.expectStatus().isOk();
	}
}