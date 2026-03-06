# Media Service

Handles image and media file storage, retrieval, and management.

## Overview

The Media Service provides comprehensive media management capabilities including file upload, storage, retrieval, and deletion. It uses MongoDB GridFS for file storage and integrates with other services via REST and Kafka.

## Features

- Image Upload (Multipart)
- Image Retrieval
- Image Deletion
- Batch Operations
- Product Image Association
- Kafka Event Consumption
- Orphaned Media Cleanup

## Technology Stack

- Spring Boot 3.x (WebFlux - Reactive)
- Spring Security
- Spring Data MongoDB (Reactive)
- Spring Kafka
- MongoDB (GridFS)
- Kafka
- Java 21

## Architecture

### Domain Model

```
Media
├── id: UUID
├── fileName: String
├── contentType: String
├── size: Long
├── filePath: String
├── status: FileStatus (PENDING, LINKED, ORPHANED)
├── productId: String (optional)
├── uploadedBy: UUID (optional)
├── createdAt: Instant
└── updatedAt: Instant
```

### File Storage
Uses MongoDB GridFS for storing binary media files:
- GridFSbucket for large file handling
- Automatic content type detection
- Unique file naming

### Ports

#### Inbound Ports
- `MediaUseCase` - Main media business logic interface

#### Outbound Ports
- `MediaRepositoryPort` - Media data persistence

## API Endpoints

### Media Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/media` | Test endpoint | Public |
| GET | `/media/{id}` | Get image by ID | Public |
| GET | `/media/product/{id}` | Get images for product | Public |
| POST | `/media` | Upload new image | Seller |
| POST | `/media/batch` | Batch image operations | Seller |
| DELETE | `/media/{id}` | Delete image | Seller |

### User Context

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/media/user` | Get current user info | Protected |

## Request/Response Models

### Upload Response
```json
{
  "id": "media-uuid",
  "fileName": "image.jpg",
  "contentType": "image/jpeg",
  "size": 102400,
  "status": "PENDING",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### Batch Image Request
```json
{
  "productId": "product-uuid",
  "imageIds": ["media-uuid-1", "media-uuid-2", "media-uuid-3"]
}
```

### Batch Image Response
```json
{
  "productId": "product-uuid",
  "linkedImageIds": ["media-uuid-1", "media-uuid-2", "media-uuid-3"]
}
```

## Kafka Integration

### Events Consumed

#### ImagesLinkedEvent
Received when images are linked to products.

```json
{
  "productId": "product-uuid",
  "imageIds": ["media-uuid-1", "media-uuid-2"],
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Topic Configuration

| Topic | Consumer Group | Description |
|-------|----------------|-------------|
| `images-linked` | media-service-group | Listens for image-product associations |

## Security

### JWT Authentication
All image management endpoints require valid JWT tokens.

### Roles
- **CLIENT** - Can view images only
- **SELLER** - Can upload, delete images

## Running Locally

```bash
cd backend/media-service
./mvnw spring-boot:run
```

### Environment Variables

```bash
export MONGO_HOST=localhost
export MONGO_PORT=27019
export MONGO_DB=media
export MONGO_USERNAME=admin
export MONGO_PASSWORD=password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export JWT_PUBLIC_KEY=your-public-key
export UPLOAD_DIR=./uploads
```

## Testing

```bash
# Upload image
curl -X POST http://localhost:8083/media \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@image.jpg"

# Get image
curl -X GET http://localhost:8083/media/media-uuid

# Get product images
curl -X GET http://localhost:8083/media/product/product-uuid

# Delete image
curl -X DELETE http://localhost:8083/media/media-uuid \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Docker

```bash
docker-compose up media-service
```

## Ports

| Service | Port |
|---------|------|
| Media Service | 8083 |
| MongoDB (Media) | 27019 |

## Orphaned Media Cleanup

The service includes a scheduled job for cleaning up orphaned media files:

```java
@Scheduled(cron = "0 0 * * * *") // Every hour
public void cleanupOrphanedMedia() { ... }
```

Criteria for orphaned media:
- No associated product
- Status is PENDING for > 24 hours

## File Storage Details

- Files stored in MongoDB GridFS
- Automatic unique filename generation
- Content-Type detection from file extension
- Maximum file size: 10MB (configurable)
