# Architecture Documentation

## System Overview

Buy-01 is a microservices-based e-commerce platform built with Spring Boot, providing product management, user authentication, and media handling capabilities.

## Architectural Style

- **Pattern**: Microservices Architecture
- **Services Architecturs**: Hexagonal Architecture
- **Communication**: REST APIs + Event-Driven (Kafka)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          Clients                                │
│                  (Web, Mobile, External APIs)                   │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       API Gateway (8080)                        │
│  • JWT Validation                                               │
│  • Rate Limiting                                                │
│  • Request Logging                                              │
│  • CORS                                                         │
│  • Route Management                                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┼───────────────┐
                ▼               ▼               ▼
┌─────────────────────┐ ┌──────────────┐ ┌─────────────────────┐
│  User Service       │ │ Product      │ │ Media Service       │
│  (8081)             │ │ Service      │ │ (8083)              │
│                     │ │ (8082)       │ │                     │
│ • Authentication    │ │              │ │ • File Upload       │
│ • User Management   │ │ • Products   │ │ • File Storage      │
│ • Registration      │ │ • Inventory  │ │ • Image Retrieval   │
│ • Login/JWT         │ │              │ │                     │
└─────────────────────┘ └──────────────┘ └─────────────────────┘
                │               │               │
                └───────────────┼───────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Discovery Server (8761)                      │
│                    Netflix Eureka Server                        │
│              Service Registration & Discovery                   │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Apache Kafka (9092)                         │
│              Event-Driven Communication                         │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                ▼                               ▼
┌──────────────────────────────┐   ┌─────────────────────────────┐
│     MongoDB Instances        │   │     Additional Services     │
├─────────────┬────────────────┤   │                             │
│ Users (27017)│Products(27018)│   │ • Logging                   │
│ Media (27019)│               │   │ • Monitoring                │
└─────────────┴────────────────┘   └─────────────────────────────┘
```

## Service Descriptions

### 1. API Gateway
- **Port**: 8080
- **Technology**: Spring Cloud Gateway
- **Responsibilities**:
  - Single entry point for all client requests
  - JWT token validation and authentication
  - Rate limiting (Bucket4j)
  - Request/response logging
  - CORS configuration
  - Dynamic service routing

### 2. Discovery Server
- **Port**: 8761
- **Technology**: Netflix Eureka
- **Responsibilities**:
  - Service registration
  - Service discovery
  - Load balancing support
  - Health monitoring

### 3. User Service
- **Port**: 8081
- **Technology**: Spring Boot (Servlet)
- **Database**: MongoDB (27017)
- **Responsibilities**:
  - User registration
  - JWT token generation
  - User profile management
  - Role-based access control

### 4. Product Service
- **Port**: 8082
- **Technology**: Spring Boot WebFlux (Reactive)
- **Database**: MongoDB (27018)
- **Responsibilities**:
  - Product CRUD operations
  - Inventory management
  - Kafka event publishing
  - Image association

### 5. Media Service
- **Port**: 8083
- **Technology**: Spring Boot WebFlux (Reactive)
- **Database**: MongoDB (27019)
- **Responsibilities**:
  - Image upload and storage (GridFS)
  - Image retrieval
  - Kafka event consumption
  - Orphaned media cleanup

## Data Flow

### Authentication Flow
```
1. User sends credentials to /auth/login
2. Gateway routes to User Service
3. User Service validates credentials
4. User Service generates JWT token
5. Token returned to client
6. Client includes token in Authorization header
7. Gateway validates token on subsequent requests
8. Gateway routes to protected services
```

### Product Creation Flow
```
1. Seller logs in and obtains JWT token
2. Seller sends POST /products with JWT
3. Gateway validates token and role
4. Gateway routes to Product Service
5. Product Service creates product
6. Product Service publishes ImagesLinkedEvent to Kafka
7. Media Service consumes event
8. Media Service updates image associations
```

## Technology Stack Details

### Core Framework
- **Spring Boot**: 3.x
- **Java**: 21

### Communication
- **REST**: Spring Web/WebFlux
- **Messaging**: Apache Kafka

### Data Storage
- **MongoDB**: Document database for all services

### Security
- **JWT**: JSON Web Tokens for authentication
- **Spring Security**: Authorization and authentication

### Service Infrastructure
- **Eureka**: Service registry and discovery
- **Spring Cloud Gateway**: API Gateway

## Design Patterns

### 1. Hexagonal Architecture (Ports & Adapters)
Each service follows hexagonal architecture:
- **Domain Layer**: Core business logic
- **Ports**: Interfaces for inbound (use cases) and outbound (repositories)
- **Adapters**: Implementations for inbound (controllers) and outbound (MongoDB)

### 2. Event-Driven Architecture
- Kafka for asynchronous communication between services
- Decouples Product Service from Media Service

### 3. API Gateway Pattern
- Centralized entry point
- Cross-cutting concerns (auth, logging, rate limiting)

### 4. Service Discovery
- Dynamic service registration
- No hardcoded service URLs

## Scalability Considerations

### Horizontal Scaling
- Each service can be scaled independently
- Stateless services for easy replication
- Session management via JWT (no sticky sessions)

### Database Scaling
- Separate MongoDB instances per service
- Service-specific databases prevent coupling

### Message Queue
- Kafka handles high-throughput event processing
- Consumer groups enable parallel processing

## Deployment

### Docker Compose
All services containerized with Docker Compose:
- Service discovery
- API Gateway
- All microservices
- MongoDB instances
- Kafka broker

### Environment Configuration
- Environment variables for sensitive data
- Profile-based configuration (dev, docker, prod)

## Error Handling

### Global Exception Handling
- Each service has a `GlobalExceptionHandler`
- Standardized error responses
- HTTP status code mapping

### Error Response Format
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Resource not found"
}
```

## Monitoring & Health

### Actuator Endpoints
All services expose health endpoints:
- `/actuator/health` - Service health status
- `/actuator/info` - Service information

### Logging
- Centralized logging structure
- Request/response logging at Gateway
- Service-specific logging
