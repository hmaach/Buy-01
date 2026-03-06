# User Service

Manages user authentication, registration, and profile management.

## Overview

The User Service handles all user-related operations including registration, login, and profile management. It provides JWT-based authentication and integrates with other services through the API Gateway.

## Features

- User Registration
- User Login (JWT Authentication)
- Profile Management
- Role-based Access Control (CLIENT, SELLER)
- Password Encryption (BCrypt)
- JWT Token Generation & Validation
- Avatar Upload Support

## Technology Stack

- Spring Boot 3.x
- Spring Security
- Spring Data MongoDB
- JWT (JSON Web Tokens)
- MongoDB
- Java 21

## Architecture

### Domain Model

```
User
├── id: UUID
├── name: String
├── email: String (unique)
├── password: String (encrypted)
├── role: Role (CLIENT, SELLER)
├── avatarUrl: String (optional)
├── createdAt: Instant
└── updatedAt: Instant
```

### Ports

#### Inbound Ports
- `UserUseCase` - Main user business logic interface
- `AuthUseCase` - Authentication business logic

#### Outbound Ports
- `UserRepositoryPort` - User data persistence
- `TokenGeneratorPort` - JWT token generation

## API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/users/auth/register` | Register new user | Public |
| POST | `/users/auth/login` | Login user | Public |

### User Management

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/users/me` | Get current user profile | Protected |
| GET | `/users/id/{id}` | Get user by ID | Protected |
| PUT | `/users/me` | Update current user profile | Protected |
| DELETE | `/users/me` | Delete current user | Protected |

## Request/Response Models

### Register Request
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "role": "SELLER"
}
```

### Login Request
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Login Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

### User Response
```json
{
  "id": "uuid",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "SELLER",
  "avatarUrl": "https://...",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

## Security

### JWT Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `JWT_PRIVATE_KEY` | Private key for signing tokens | - |
| `JWT_PUBLIC_KEY` | Public key for verifying tokens | - |
| `JWT_EXPIRATION` | Token expiration time (ms) | 3600000 |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | 86400000 |

### Roles

- **CLIENT** - Standard customer access
- **SELLER** - Seller/merchant access (can create products)

## Running Locally

```bash
cd backend/user-service
./mvnw spring-boot:run
```

### Environment Variables

```bash
export MONGO_HOST=localhost
export MONGO_PORT=27017
export MONGO_DB=users
export MONGO_USERNAME=admin
export MONGO_PASSWORD=password
export JWT_PRIVATE_KEY=your-private-key
export JWT_PUBLIC_KEY=your-public-key
export JWT_EXPIRATION=3600000
export JWT_REFRESH_EXPIRATION=86400000
```

## Testing

```bash
# Register a user
curl -X POST http://localhost:8081/users/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "role": "SELLER"
  }'

# Login
curl -X POST http://localhost:8081/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'

# Get profile
curl -X GET http://localhost:8081/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Docker

```bash
docker-compose up user-service
```

## Ports

| Service | Port |
|---------|------|
| User Service | 8081 |
| MongoDB | 27017 |

## Integration with Media Service

The User Service can upload avatars to the Media Service:

```java
@PostMapping(value = "/media/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
UUID uploadAvatar(@RequestPart("file") MultipartFile file);
```
