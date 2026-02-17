# Gateway Service

API Gateway for the e-commerce microservices platform.

## Features

- ✅ Single entry point for all services
- ✅ JWT authentication validation
- ✅ Role-based access control (CLIENT, SELLER)
- ✅ CORS configuration
- ✅ Service discovery integration (Eureka)
- ✅ Request/response logging
- ✅ Centralized error handling

## Routes

### User Service

- `POST /auth/register` - Public (registration)
- `POST /auth/login` - Public (login)
- `GET /me` - Protected (get profile)
- `PUT /me` - Protected (update profile)

### Product Service

- `GET /products` - Public (list products)
- `GET /products/{id}` - Public (get product)
- `POST /products` - Seller only (create product)
- `PUT /products/{id}` - Seller only (update product)
- `DELETE /products/{id}` - Seller only (delete product)

### Media Service

- `GET /media/images/{id}` - Public (view image)
- `POST /media/images` - Seller only (upload image)
- `DELETE /media/images/{id}` - Seller only (delete image)

## Running Locally

```bash
# 1. Start Eureka Discovery Service
cd ../discovery-service
mvn spring-boot:run

# 2. Start User Service
cd ../user-service
mvn spring-boot:run

# 3. Start Gateway
cd ../gateway-service
mvn spring-boot:run
```

Access gateway at: `http://localhost:8080`

## Testing

```bash
# Register a user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "role": "SELLER"
  }'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'

# Get profile (use token from login response)
curl -X GET http://localhost:8080/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Environment Variables

- `JWT_SECRET` - Secret key for JWT validation (must match User Service)
- `EUREKA_URL` - Eureka server URL
- `SERVER_PORT` - Gateway port (default: 8080)
