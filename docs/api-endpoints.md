# API Endpoints Documentation

## Base URL

```
http://localhost:8080
```

All API requests should be directed through the API Gateway.

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <jwt_token>
```

## Response Format

### Success Response
```json
{
  "data": { ... }
}
```

### Error Response
```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Resource not found"
}
```

---

## User Service Endpoints

### Authentication

#### Register User
```
POST /auth/register
```

Register a new user in the system.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "role": "SELLER"
}
```

**Parameters:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | User's full name |
| email | String | Yes | User's email (unique) |
| password | String | Yes | Password (min 8 chars) |
| role | String | Yes | CLIENT or SELLER |

**Response (201 Created):**
```json
{
  "id": "uuid",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "SELLER",
  "avatarUrl": null,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

#### Login User
```
POST /auth/login
```

Authenticate a user and receive JWT tokens.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600
}
```

---

### User Management

#### Get Current User Profile
```
GET /me
```

Get the profile of the currently authenticated user.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
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

---

#### Get User by ID
```
GET /users/id/{id}
```

Get a user by their UUID.

**Parameters:**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | User's unique identifier |

**Response (200 OK):**
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

---

#### Update Current User
```
PUT /users/me
```

Update the currently authenticated user's profile.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Content-Type:** multipart/form-data

**Request Parts:**
| Field | Type | Description |
|-------|------|-------------|
| name | String | User's full name |
| file | File | Profile avatar image (optional) |

**Response (200 OK):**
```json
{
  "id": "uuid",
  "name": "John Updated",
  "email": "john@example.com",
  "role": "SELLER",
  "avatarUrl": "https://...",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

#### Delete Current User
```
DELETE /users/me
```

Delete the currently authenticated user's account.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (204 No Content)**

---

## Product Service Endpoints

### Product Management

#### List Products
```
GET /products
```

Get a list of all products.

**Query Parameters:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| beforeTime | Instant | No | Filter products created before this time |

**Response (200 OK):**
```json
{
  "products": [
    {
      "id": "product-uuid",
      "sellerId": "user-uuid",
      "name": "iPhone 15 Pro",
      "description": "Latest iPhone",
      "price": 999.99,
      "quantity": 100,
      "category": "Electronics",
      "imageIds": ["media-uuid-1"],
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

---

#### Get Product by ID
```
GET /products/{id}
```

Get a specific product by ID.

**Parameters:**
| Field | Type | Description |
|-------|------|-------------|
| id | String | Product's unique identifier |

**Response (200 OK):**
```json
{
  "id": "product-uuid",
  "sellerId": "user-uuid",
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone",
  "price": 999.99,
  "quantity": 100,
  "category": "Electronics",
  "imageIds": ["media-uuid-1"],
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

---

#### Create Product
```
POST /products
```

Create a new product. **Requires SELLER role.**

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 chip",
  "price": 999.99,
  "quantity": 100,
  "category": "Electronics"
}
```

**Parameters:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Product name |
| description | String | Yes | Product description |
| price | BigDecimal | Yes | Product price |
| quantity | Integer | Yes | Available quantity |
| category | String | Yes | Product category |

**Response (201 Created):**
```json
{
  "id": "product-uuid",
  "sellerId": "user-uuid",
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone with A17 chip",
  "price": 999.99,
  "quantity": 100,
  "category": "Electronics",
  "imageIds": [],
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

---

#### Update Product
```
PUT /products/{id}
```

Update an existing product. **Requires SELLER role (owner only).**

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "iPhone 15 Pro Updated",
  "description": "Updated description",
  "price": 899.99,
  "quantity": 50,
  "category": "Electronics"
}
```

**Response (200 OK):**
```json
{
  "id": "product-uuid",
  "sellerId": "user-uuid",
  "name": "iPhone 15 Pro Updated",
  "description": "Updated description",
  "price": 899.99,
  "quantity": 50,
  "category": "Electronics",
  "imageIds": [],
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-02T00:00:00Z"
}
```

---

#### Delete Product
```
DELETE /products/{id}
```

Delete a product. **Requires SELLER role (owner only).**

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (204 No Content)**

---

## Media Service Endpoints

### Media Management

#### Upload Image
```
POST /media
```

Upload a new image. **Requires SELLER role.**

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data
```

**Request Parts:**
| Field | Type | Description |
|-------|------|-------------|
| file | File | Image file (JPEG, PNG, etc.) |

**Response (201 Created):**
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

---

#### Get Image
```
GET /media/{id}
```

Get an image by ID. Returns the raw image data.

**Parameters:**
| Field | Type | Description |
|-------|------|-------------|
| id | String | Media's unique identifier |

**Response:** Raw image data (JPEG, PNG, etc.)

---

#### Get Product Images
```
GET /media/product/{productId}
```

Get all images associated with a product.

**Parameters:**
| Field | Type | Description |
|-------|------|-------------|
| productId | String | Product's unique identifier |

**Response (200 OK):**
```json
{
  "productId": "product-uuid",
  "imageUrls": [
    "http://localhost:8083/media/media-uuid-1",
    "http://localhost:8083/media/media-uuid-2"
  ]
}
```

---

#### Delete Image
```
DELETE /media/{id}
```

Delete an image. **Requires SELLER role.**

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (204 No Content)**

---

#### Batch Image Operation
```
POST /media/batch
```

Link multiple images to a product. **Requires SELLER role.**

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "productId": "product-uuid",
  "imageIds": ["media-uuid-1", "media-uuid-2", "media-uuid-3"]
}
```

**Response (200 OK):**
```json
{
  "productId": "product-uuid",
  "linkedImageIds": ["media-uuid-1", "media-uuid-2", "media-uuid-3"]
}
```

---

## Health Check Endpoints

### API Gateway
```
GET /actuator/health
```

### User Service
```
GET http://localhost:8081/actuator/health
```

### Product Service
```
GET http://localhost:8082/actuator/health
```

### Media Service
```
GET http://localhost:8083/actuator/health
```

---

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Request successful, no response body |
| 400 | Bad Request - Invalid request data |
| 401 | Unauthorized - Invalid or missing JWT token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error - Server error |

---

## Rate Limiting

The API Gateway implements rate limiting:

- **Limit**: 100 requests per minute (configurable)
- **Headers**:
  - `X-RateLimit-Limit`: Maximum requests allowed
  - `X-RateLimit-Remaining`: Remaining requests in window
  - `Retry-After`: Seconds to wait if rate limited
