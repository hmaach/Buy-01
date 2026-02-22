package com.buy01.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.config.TopicBuilder;

import com.buy01.product.domain.ports.inbound.ProductUseCase;
import com.buy01.product.domain.ports.outbound.ProductRepositoryPort;
import com.buy01.product.domain.service.ProductServiceImpl;

@Configuration
public class BeanConfiguration {


    @Bean
    public ProductUseCase productUseCase(ProductRepositoryPort repository) {
        return new ProductServiceImpl(repository);
    }

    @Bean
    CommandLineRunner checkMongo(MongoTemplate mongoTemplate) {
        return args -> {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
            System.out.println("MongoDB status: Connected");
        };
    }

}
