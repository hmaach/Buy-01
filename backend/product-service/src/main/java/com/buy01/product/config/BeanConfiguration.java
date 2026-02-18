package com.buy01.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;
import com.buy01.product.domain.service.ProductServiceImpl;

@Configuration
public class BeanConfiguration {
    @Bean
    public ProductUseCase productUseCase(ProductRepositoryPort repository) {
        return new ProductServiceImpl(repository);
    }
}
