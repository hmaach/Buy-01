package com.buy01.product.infrastructure.config;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeanConfiguration {

    @Value("${services.media}")
    public String mediaServiceUrl;

    @Bean
    public WebClient mediaWebClient() {
        return WebClient.builder()
                .baseUrl(mediaServiceUrl)
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
