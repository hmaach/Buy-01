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

## Security Setup (HTTPS)

### using keytool
To run the Gateway, you must generate a local SSL certificate:

```bash
keytool -genkeypair -alias springgateway -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/edge-service.p12 -validity 3650 -storepass yourpassword
```
- Note: The .p12 file is ignored by git for security.

### using openSSL
#### Step A: Generate the Private Key and Certificate

Run this in your api-gateway terminal:
```bash
openssl req -x509 -newkey rsa:4096 -keyout src/main/resources/key.pem -out src/main/resources/cert.pem -sha256 -days 365 -nodes
```
This gives you key.pem (your secret) and cert.pem (your public certificate).

#### Step B: Bundle them into a .p12 file (What Spring Needs)

Spring Boot cannot read raw .pem files easily without extra code. You need to "wrap" them:
```Bash
openssl pkcs12 -export -in src/main/resources/cert.pem -inkey src/main/resources/key.pem -out src/main/resources/edge-service.p12 -name springgateway -passout pass:123456
```
Important: The -name springgateway here is the Alias. It must match the key-alias in your application.yml.
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
cd ../discovery-server
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
