package com.buy01.product.infrastructure.config;

import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeanConfiguration {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient mediaWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://MEDIA-SERVICE")
                .build();
    }

    @Bean
    CommandLineRunner checkMongo(MongoTemplate mongoTemplate) {
        return args -> {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
            System.out.println("MongoDB status: Connected");
        };
    }
}
