# Kafka Documentation

## Overview

The Buy-01 platform uses Apache Kafka for event-driven communication between microservices. Kafka enables asynchronous messaging, decoupling the Product Service from the Media Service.

## Architecture

```
┌──────────────────┐         ┌──────────────────┐
│  Product Service │         │  Media Service   │
│                  │         │                  │
│  Producer        │         │  Consumer        │
│      │           │         │      │           │
│      ▼           │         │      ▼           │
│ ┌─────────┐      │         │ ┌─────────┐      │
│ │ Kafka   │      │────────▶│ │ Kafka   │      │
│ │Producer │      │         │ │Listener │      │
│ └─────────┘      │         │ └─────────┘      │
└──────────────────┘         └──────────────────┘
         │                            │
         └──────────────┬─────────────┘
                        ▼
              ┌──────────────────┐
              │ Apache Kafka     │
              │ Broker:9092      │
              │                  │
              │ Topics:          │
              │ - images-linked  │
              └──────────────────┘
```

## Kafka Topics

### images-linked

**Description**: Published when images are linked to products.

**Publisher**: Product Service

**Subscriber**: Media Service

**Purpose**: Notifies the Media Service when product images should be associated with a product.

---

## Event Schema

### ImagesLinkedEvent

```json
{
  "productId": "product-uuid",
  "imageIds": ["media-uuid-1", "media-uuid-2"],
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**Fields:**

| Field | Type | Description |
|-------|------|-------------|
| productId | String | Unique identifier of the product |
| imageIds | List<String> | List of media IDs to link |
| timestamp | Instant | Event creation time |

---

## Implementation

### Product Service - Producer

**Configuration** (`KafkaProducerConfig.java`):

```java
@Bean
public ProducerFactory<String, ImagesLinkedEvent> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
}

@Bean
public KafkaTemplate<String, ImagesLinkedEvent> kafkaTemplate(
        ProducerFactory<String, ImagesLinkedEvent> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
}
```

**Publishing Events**:

```java
@Autowired
private KafkaTemplate<String, ImagesLinkedEvent> kafkaTemplate;

public void publishImagesLinkedEvent(String productId, List<String> imageIds) {
    ImagesLinkedEvent event = new ImagesLinkedEvent(productId, imageIds, Instant.now());
    kafkaTemplate.send("images-linked", productId, event);
}
```

---

### Media Service - Consumer

**Configuration** (`KafkaConsumerConfig.java`):

```java
@Bean
public ConsumerFactory<String, ImagesLinkedEvent> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "media-service-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
            new JsonDeserializer<>(ImagesLinkedEvent.class));
}

@Bean
public ConcurrentKafkaListenerContainerFactory<String, ImagesLinkedEvent> 
        kafkaListenerContainerFactory(ConsumerFactory<String, ImagesLinkedEvent> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<String, ImagesLinkedEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setConcurrency(2);
    return factory;
}
```

**Consuming Events**:

```java
@KafkaListener(
    topics = "${kafka.topics.images-linked:images-linked}", 
    groupId = "media-service-group", 
    containerFactory = "kafkaListenerContainerFactory"
)
public void onImagesLinked(ImagesLinkedEvent event) {
    // Update media records to link with product
    mediaService.linkImagesToProduct(event.getProductId(), event.getImageIds());
}
```

---

## Kafka Configuration

### Docker Compose Configuration

```yaml
kafka:
  image: apache/kafka:latest
  ports:
    - "9092:9092"
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
    KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_NUM_PARTITIONS: 3
```

### Application Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ${SERVICE_NAME}-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

kafka:
  topics:
    images-linked: images-linked
```

---

## Message Flow

### Image Linking Flow

```
1. Seller uploads images via Media Service
   POST /media
   → Returns media IDs: [media-uuid-1, media-uuid-2]

2. Seller creates product with image IDs
   POST /products
   Body: { "name": "...", "imageIds": ["media-uuid-1", "media-uuid-2"] }
   → Product created

3. Product Service publishes ImagesLinkedEvent
   Topic: images-linked
   Key: product-uuid
   Value: { "productId": "...", "imageIds": [...], "timestamp": "..." }

4. Media Service consumes event
   @KafkaListener(topics = "images-linked")

5. Media Service updates image associations
   - Updates each media record's productId
   - Updates status to LINKED
```

---

## Consumer Groups

| Service | Group ID | Purpose |
|---------|----------|---------|
| Media Service | media-service-group | Consume image linking events |

**Benefits:**
- Multiple instances of Media Service can share the load
- Each message is processed only once within the group
- Horizontal scalability

---

## Error Handling

### Producer Error Handling

```java
ListenableFuture<SendResult<String, ImagesLinkedEvent>> future = 
    kafkaTemplate.send("images-linked", productId, event);

future.addCallback(new ListenableFutureCallback<SendResult<String, ImagesLinkedEvent>>() {
    @Override
    public void onSuccess(SendResult<String, ImagesLinkedEvent> result) {
        // Log success
    }
    
    @Override
    public void onFailure(Throwable ex) {
        // Handle failure - retry or log
    }
});
```

### Consumer Error Handling

```java
@KafkaListener(topics = "images-linked")
public void onImagesLinked(ImagesLinkedEvent event) {
    try {
        mediaService.linkImagesToProduct(event.getProductId(), event.getImageIds());
    } catch (Exception e) {
        // Handle error - could implement retry or dead letter queue
        log.error("Failed to process images linked event", e);
    }
}
```

---

## Monitoring

### Kafka Topics

List topics:
```bash
docker exec -it kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

Describe topic:
```bash
docker exec -it kafka kafka-topics.sh --describe --topic images-linked --bootstrap-server localhost:9092
```

### Consumer Groups

List consumer groups:
```bash
docker exec -it kafka kafka-consumer-groups.sh --list --bootstrap-server localhost:9092
```

View consumer offsets:
```bash
docker exec -it kafka kafka-consumer-groups.sh --group media-service-group --describe --bootstrap-server localhost:9092
```

---

## Performance Considerations

### Partitioning

- **Partitions**: 3 (configured in Docker Compose)
- **Key**: Product ID ensures messages for same product go to same partition
- **Ordering**: Messages for a product are processed in order

### Concurrency

```java
factory.setConcurrency(2); // Media Service
```

Multiple consumer threads process partitions in parallel.

---

## Future Enhancements

- Add dead letter queue (DLQ) for failed messages
- Implement message retry with exponential backoff
- Add message schema registry (Confluent Schema Registry)
- Implement exactly-once semantics
- Add message encryption for sensitive data
