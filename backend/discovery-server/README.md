# Discovery Server (Eureka)

Service registry and discovery server for the microservices architecture.

## Overview

The Discovery Server uses Netflix Eureka for service registration and discovery. All microservices register themselves with this server, allowing for dynamic service location without hardcoded URLs.

## Features

- Service Registration
- Service Discovery
- Load Balancing Support
- Health Monitoring
- RESTful Management API

## Technology Stack

- Spring Boot 3.x
- Netflix Eureka Server
- Java 21

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Service port | 8761 |
| `eureka.instance.hostname` | Eureka hostname | localhost |

## Running Locally

```bash
cd backend/discovery-server
./mvnw spring-boot:run
```

Or use Docker:

```bash
docker-compose up discovery-server
```

## Health Check

```
GET http://localhost:8761/actuator/health
```

## Eureka Dashboard

Access the Eureka dashboard at: `http://localhost:8761`

## Service Registration

Other services register with Eureka using:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
  instance:
    prefer-ip-address: true
```

## Ports

| Service | Port |
|---------|------|
| Discovery Server | 8761 |
