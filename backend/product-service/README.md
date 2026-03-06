# Product Service

Manages product catalog, inventory, and product-related operations.

## Overview

The Product Service handles product management including CRUD operations, inventory tracking, and image associations. It integrates with the Media Service for image handling and uses Kafka for asynchronous event publishing.

## Features

- Product CRUD Operations
- Inventory Management
- Product Search & Filtering
- Image Association with Media Service
- Kafka Event Publishing
- Role-based Access Control (SELLER only can manage products)

## Technology Stack

- Spring Boot 3.x (WebFlux - Reactive)
- Spring Security
- Spring Data MongoDB (Reactive)
- Spring Kafka
- MongoDB
- Kafka
- Java 21

## Architecture

### Domain Model

```
Product
├── id: String
├── sellerId: UUID
├── name: String
├── description: String
├── price: BigDecimal
├── quantity: Integer
├── category: String
├── imageIds: List<String>
├── status: ProductStatus (ACTIVE, INACTIVE, DELETED)
├── createdAt: Instant
└── updatedAt: Instant
```

### Ports

#### Inbound Ports
- `ProductUseCase` - Main product business logic interface

#### Outbound Ports
- `ProductRepositoryPort` - Product data persistence

## API Endpoints

### Product Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/products` | List all products | Public |
| GET | `/products/{id}` | Get product by ID | Public |
| POST | `/products` | Create new product | Seller |
| PUT | `/products/{id}` | Update product | Seller |
| DELETE | `/products/{id}` | Delete product | Seller |

### User Context

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/products/user` | Get current user info | Protected |

## Request/Response Models

### Create Product Request
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 chip",
  "price": 999.99,
  "quantity": 100,
  "category": "Electronics"
}
```

### Product Response
```json
{
  "id": "product-uuid",
  "sellerId": "user-uuid",
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 chip",
  "price": 999.99,
  "quantity": 100,
  "category": "Electronics",
  "imageIds": ["media-uuid-1", "media-uuid-2"],
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

## Kafka Integration

### Events Published

#### ImagesLinkedEvent
Published when product images are linked to a product.

```json
{
  "productId": "product-uuid",
  "imageIds": ["media-uuid-1", "media-uuid-2"],
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Topic Configuration

| Topic | Description |
|-------|-------------|
| `images-linked` | Published when images are linked to products |

## Security

### JWT Authentication
All product management endpoints require valid JWT tokens.

### Roles
- **CLIENT** - Can view products only
- **SELLER** - Can create, update, delete products

### Endpoint Protection

```java
@PreAuthorize("hasRole('SELLER')")
@PostMapping
public Mono<ResponseEntity<?>> createProduct(...)
```

## Running Locally

```bash
cd backend/product-service
./mvnw spring-boot:run
```

### Environment Variables

```bash
export MONGO_HOST=localhost
export MONGO_PORT=27018
export MONGO_DB=products
export MONGO_USERNAME=admin
export MONGO_PASSWORD=password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export JWT_PUBLIC_KEY=your-public-key
```

## Testing

```bash
# Create product (requires auth)
curl -X POST http://localhost:8082/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "iPhone 15 Pro",
    "description": "Latest iPhone",
    "price": 999.99,
    "quantity": 100,
    "category": "Electronics"
  }'

# Get products
curl -X GET http://localhost:8082/products

# Get product by ID
curl -X GET http://localhost:8082/products/product-uuid
```

## Docker

```bash
docker-compose up product-service
```

## Ports

| Service | Port |
|---------|------|
| Product Service | 8082 |
| MongoDB (Product) | 27018 |

## Integration with Media Service

Products store references to media images. When images are uploaded:
1. Media Service stores the image
2. Media Service publishes `ImagesLinkedEvent` to Kafka
3. Product Service consumes the event and updates product's imageIds
