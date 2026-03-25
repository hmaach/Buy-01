package com.buy01.product.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.buy01.product.domain.model.Product;
import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.infrastructure.config.SecurityConfig;
import com.buy01.product.infrastructure.security.JwtAuthenticationFilter;
import com.buy01.product.infrastructure.security.JwtUtil;
import com.buy01.product.infrastructure.web.dto.ProductList;
import com.buy01.product.infrastructure.web.dto.ProductResponse;
import com.buy01.product.infrastructure.web.exception.GlobalExceptionHandler;
import com.buy01.product.infrastructure.web.mapper.ProductWebMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(ProductController.class)
@Import({
                ProductWebMapper.class,
                GlobalExceptionHandler.class,
                SecurityConfig.class,
                JwtAuthenticationFilter.class
})
class ProductControllerTest {

        private static final String SELLER_TOKEN = "seller-token";
        private static final String USER_TOKEN = "user-token";

        @Autowired
        private WebTestClient webTestClient;

        @MockitoBean
        private ProductUseCase productUseCase;

        @MockitoBean
        private JwtUtil jwtUtil;

        @Test
        void createProductReturnsCreatedProductForAuthenticatedSeller() {
                Instant now = Instant.parse("2026-03-25T12:00:00Z");
                Product createdProduct = Product.builder()
                                .id("prod-1")
                                .name("Gaming Mouse")
                                .description("High precision wireless gaming mouse")
                                .price(199)
                                .quantity(5)
                                .userId("seller-1")
                                .imagesIds(List.of("img-1", "img-2"))
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                mockToken(SELLER_TOKEN, "seller-1", "SELLER");
                given(productUseCase.createProduct(any(Product.class), eq(List.of("img-1", "img-2"))))
                                .willReturn(Mono.just(createdProduct));

                webTestClient.post()
                                .uri("/products")
                                .header(HttpHeaders.AUTHORIZATION, bearer(SELLER_TOKEN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("""
                                                {
                                                  "name": "Gaming Mouse",
                                                  "description": "High precision wireless gaming mouse",
                                                  "price": 199,
                                                  "quantity": 5,
                                                  "imagesIds": ["img-1", "img-2"]
                                                }
                                                """)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody()
                                .jsonPath("$.id").isEqualTo("prod-1")
                                .jsonPath("$.name").isEqualTo("Gaming Mouse")
                                .jsonPath("$.userId").isEqualTo("seller-1")
                                .jsonPath("$.imagesIds[0]").isEqualTo("img-1");

                verify(productUseCase).createProduct(
                                org.mockito.ArgumentMatchers.argThat(product -> "seller-1".equals(product.getUserId())
                                                && "Gaming Mouse".equals(product.getName())
                                                && product.getQuantity() == 5),
                                eq(List.of("img-1", "img-2")));
        }

        @Test
        void createProductReturnsBadRequestWhenPayloadIsInvalid() {
                mockToken(SELLER_TOKEN, "seller-1", "SELLER");

                webTestClient.post()
                                .uri("/products")
                                .header(HttpHeaders.AUTHORIZATION, bearer(SELLER_TOKEN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("""
                                                {
                                                  "name": "",
                                                  "description": "short",
                                                  "price": 0,
                                                  "quantity": -1,
                                                  "imagesIds": []
                                                }
                                                """)
                                .exchange()
                                .expectStatus().isBadRequest()
                                .expectBody()
                                .jsonPath("$.title").isEqualTo("Validation Failed")
                                .jsonPath("$.detail").value(detail -> {
                                        String text = (String) detail;
                                        org.assertj.core.api.Assertions.assertThat(text)
                                                        .contains("name")
                                                        .contains("description")
                                                        .contains("price")
                                                        .contains("quantity")
                                                        .contains("imagesIds");
                                });
        }

        @Test
        void updateProductReturnsUpdatedProductForAuthenticatedSeller() {
                Instant now = Instant.parse("2026-03-25T12:00:00Z");
                Product updatedProduct = Product.builder()
                                .id("prod-9")
                                .name("Updated Keyboard")
                                .description("Mechanical keyboard with tactile switches")
                                .price(149)
                                .quantity(7)
                                .userId("seller-1")
                                .imagesIds(List.of("img-9"))
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                mockToken(SELLER_TOKEN, "seller-1", "SELLER");
                given(productUseCase.updateProduct(any(Product.class), eq("prod-9"), eq("seller-1")))
                                .willReturn(Mono.just(updatedProduct));

                webTestClient.put()
                                .uri("/products/prod-9")
                                .header(HttpHeaders.AUTHORIZATION, bearer(SELLER_TOKEN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("""
                                                {
                                                  "name": "Updated Keyboard",
                                                  "description": "Mechanical keyboard with tactile switches",
                                                  "price": 149,
                                                  "quantity": 7,
                                                  "imagesIds": ["img-9"]
                                                }
                                                """)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.id").isEqualTo("prod-9")
                                .jsonPath("$.name").isEqualTo("Updated Keyboard");
        }

        @Test
        void getProductReturnsProductWhenFound() {
                Instant now = Instant.parse("2026-03-25T12:00:00Z");
                ProductResponse response = ProductResponse.builder()
                                .id("prod-2")
                                .name("Phone")
                                .description("Latest generation phone with OLED display")
                                .price(999)
                                .quantity(3)
                                .userId("seller-2")
                                .imagesIds(List.of("img-3"))
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                given(productUseCase.getProductWithImages("prod-2")).willReturn(Mono.just(response));

                webTestClient.get()
                                .uri("/products/prod-2")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.id").isEqualTo("prod-2")
                                .jsonPath("$.name").isEqualTo("Phone");
        }

        @Test
        void getProductReturnsNotFoundWhenMissing() {
                given(productUseCase.getProductWithImages("missing")).willReturn(Mono.empty());

                webTestClient.get()
                                .uri("/products/missing")
                                .exchange()
                                .expectStatus().isNotFound()
                                .expectBody()
                                .jsonPath("$.detail").isEqualTo("Product with ID missing not found");
        }

        @Test
        void getProductsListReturnsProducts() {
                Instant createdAt = Instant.parse("2026-03-25T10:00:00Z");
                ProductList product = new ProductList("prod-3", "Laptop", 1299, createdAt, "img-main");
                given(productUseCase.getProductsList(any(Instant.class))).willReturn(Flux.just(product));

                webTestClient.get()
                                .uri("/products")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$[0].id").isEqualTo("prod-3")
                                .jsonPath("$[0].name").isEqualTo("Laptop")
                                .jsonPath("$[0].image").isEqualTo("img-main");
        }

        @Test
        void getUserProductsListReturnsCurrentUsersProducts() {
                Instant createdAt = Instant.parse("2026-03-25T10:00:00Z");
                ProductList product = new ProductList("prod-4", "Camera", 499, createdAt, "img-camera");

                mockToken(USER_TOKEN, "user-55", "USER");
                given(productUseCase.getUserProductsList(eq("user-55"), any(Instant.class)))
                                .willReturn(Flux.just(product));

                webTestClient.get()
                                .uri("/products/user")
                                .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN))
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$[0].id").isEqualTo("prod-4")
                                .jsonPath("$[0].name").isEqualTo("Camera");
        }

        @Test
        void deleteProductReturnsNoContentForAuthenticatedSeller() {
                mockToken(SELLER_TOKEN, "seller-1", "SELLER");
                given(productUseCase.deleteProduct("prod-7", "seller-1")).willReturn(Mono.empty());

                webTestClient.delete()
                                .uri("/products/prod-7")
                                .header(HttpHeaders.AUTHORIZATION, bearer(SELLER_TOKEN))
                                .exchange()
                                .expectStatus().isNoContent();
        }

        @Test
        void createProductRejectsNonSellerUser() {
                mockToken(USER_TOKEN, "user-10", "USER");

                webTestClient.post()
                                .uri("/products")
                                .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("""
                                                {
                                                  "name": "Gaming Mouse",
                                                  "description": "High precision wireless gaming mouse",
                                                  "price": 199,
                                                  "quantity": 5,
                                                  "imagesIds": ["img-1"]
                                                }
                                                """)
                                .exchange()
                                .expectStatus().isForbidden();
        }

        private void mockToken(String token, String userId, String role) {
                given(jwtUtil.validateToken(token)).willReturn(true);
                given(jwtUtil.extractUserId(token)).willReturn(userId);
                given(jwtUtil.extractEmail(token)).willReturn(userId + "@buy01.test");
                given(jwtUtil.extractRole(token)).willReturn(role);
        }

        private String bearer(String token) {
                return "Bearer " + token;
        }
}
