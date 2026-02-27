package com.buy01.product.infrastructure.config;

import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class BeanConfiguration {

    @Bean
    CommandLineRunner checkMongo(MongoTemplate mongoTemplate) {
        return args -> {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
            System.out.println("MongoDB status: Connected");
        };
    }

}
