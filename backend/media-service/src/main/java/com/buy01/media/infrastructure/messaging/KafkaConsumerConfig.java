package com.buy01.media.infrastructure.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.buy01.media.infrastructure.web.dto.ImagesLinkedEvent;
import com.buy01.media.infrastructure.web.dto.ProductDeletedEvent;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "media-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        

        return props;
    }

    @Bean
    public ConsumerFactory<String, ImagesLinkedEvent> consumerFactory() {
        JsonDeserializer<ImagesLinkedEvent> deserializer = new JsonDeserializer<>(ImagesLinkedEvent.class, false);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ImagesLinkedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ImagesLinkedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory()); // Call the bean method directly
        factory.setConcurrency(2);
        return factory;
    }

    // ────────────────────────────────────────────────
    // ProductDeletedEvent
    // ────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, ProductDeletedEvent> productDeletedConsumerFactory() {
        JsonDeserializer<ProductDeletedEvent> deserializer = new JsonDeserializer<>(ProductDeletedEvent.class, false);
        deserializer.addTrustedPackages("*");
        
        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductDeletedEvent> productDeletedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductDeletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productDeletedConsumerFactory());
        factory.setConcurrency(2);
        return factory;
    }
}