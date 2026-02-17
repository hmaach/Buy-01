# Gateway Service - Testing Guide

## 🧪 Testing Checklist

### Prerequisites
- [ ] Eureka Discovery Service running on port 8761
- [ ] User Service running on port 8081
- [ ] Gateway Service running on port 8080

---

## 1️⃣ Health Check Tests

### Test 1: Gateway Health
```bash
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Test 2: View All Routes
```bash
curl http://localhost:8080/actuator/gateway/routes
```

**Expected:** JSON array of all configured routes

---

## 2️⃣ Public Route Tests (No Authentication)

### Test 3: User Registration
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Seller",
    "email": "seller@test.com",
    "password": "SecurePass123",
    "role": "SELLER"
  }'
```

**Expected Response:**
```json
{
  "id": "...",
  "name": "Test Seller",
  "email": "seller@test.com",
  "role": "SELLER"
}
```

### Test 4: User Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@test.com",
    "password": "SecurePass123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "...",
    "name": "Test Seller",
    "email": "seller@test.com",
    "role": "SELLER"
  }
}
```

**⚠️ Save the token for next tests!**

---

## 3️⃣ Protected Route Tests (Authentication Required)

### Test 5: Get Profile WITHOUT Token (Should Fail)
```bash
curl -X GET http://localhost:8080/me
```

**Expected Response:**
```json
{
  "error": "Missing authorization header",
  "status": 401
}
```

### Test 6: Get Profile WITH Token (Should Succeed)
```bash
# Replace YOUR_TOKEN with the token from login
curl -X GET http://localhost:8080/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "id": "...",
  "name": "Test Seller",
  "email": "seller@test.com",
  "role": "SELLER"
}
```

### Test 7: Get Profile WITH Invalid Token (Should Fail)
```bash
curl -X GET http://localhost:8080/me \
  -H "Authorization: Bearer INVALID_TOKEN"
```

**Expected Response:**
```json
{
  "error": "Invalid or expired token: ...",
  "status": 401
}
```

---

## 4️⃣ Role-Based Access Tests

### Test 8: Create Product as SELLER (Should Succeed)
```bash
# First, login as SELLER and get token
# Then use the token:

curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SELLER_TOKEN" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99
  }'
```

**Expected:** Product created successfully

### Test 9: Create Product as CLIENT (Should Fail)
```bash
# First, register and login as CLIENT
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Client",
    "email": "client@test.com",
    "password": "SecurePass123",
    "role": "CLIENT"
  }'

curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@test.com",
    "password": "SecurePass123"
  }'

# Try to create product with CLIENT token:
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer CLIENT_TOKEN" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99
  }'
```

**Expected Response:**
```json
{
  "error": "Insufficient permissions",
  "status": 403
}
```

---

## 5️⃣ CORS Tests

### Test 10: Preflight Request
```bash
curl -X OPTIONS http://localhost:8080/auth/login \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v
```

**Expected Headers:**
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: Authorization, Content-Type, Accept, Origin, X-Requested-With
Access-Control-Allow-Credentials: true
```

---

## 6️⃣ Request Logging Tests

### Test 11: Check Logs
After making any request, check the gateway logs:

```bash
# In your terminal where gateway is running, you should see:

========== Incoming Request ==========
Method: POST
Path: /auth/login
Headers: {content-type=[application/json], ...}
======================================

========== Outgoing Response ==========
Status: 200 OK
=======================================
```

---

## 7️⃣ Error Handling Tests

### Test 12: Invalid Route
```bash
curl http://localhost:8080/invalid-route
```

**Expected:** 404 Not Found

### Test 13: Service Down
```bash
# Stop User Service
# Then try:
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@test.com",
    "password": "test"
  }'
```

**Expected:** 503 Service Unavailable or Connection Error

---

## 8️⃣ Load Balancing Test (Optional)

If you run multiple instances of User Service:

```bash
# Start User Service on different ports
# Port 8081
java -jar user-service.jar --server.port=8081

# Port 8082
java -jar user-service.jar --server.port=8082

# Make multiple requests
for i in {1..10}; do
  curl -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "email": "test@test.com",
      "password": "test"
    }'
done
```

**Expected:** Requests distributed between instances (check logs)

---

## 🐛 Troubleshooting

### Problem: "Connection refused"
**Solution:** 
- Check if Eureka is running: `curl http://localhost:8761`
- Check if User Service is registered: `http://localhost:8761` (browser)

### Problem: "Invalid token"
**Solution:**
- Verify JWT secret matches User Service
- Check token hasn't expired (default 24 hours)
- Ensure Bearer prefix is included

### Problem: "CORS error" in browser
**Solution:**
- Add your frontend URL to `CorsConfig.java`
- Check browser console for exact error
- Verify preflight (OPTIONS) request succeeds

### Problem: "Service not found"
**Solution:**
- Wait 30 seconds for service registration
- Check Eureka dashboard: `http://localhost:8761`
- Verify service name matches route config

---

## 📊 Expected Test Results Summary

| Test | Expected Result |
|------|----------------|
| Health check | ✅ Status: UP |
| Register user | ✅ 201 Created |
| Login | ✅ 200 OK with token |
| Get profile without token | ❌ 401 Unauthorized |
| Get profile with token | ✅ 200 OK |
| Create product as SELLER | ✅ 201 Created |
| Create product as CLIENT | ❌ 403 Forbidden |
| CORS preflight | ✅ Correct headers |
| Invalid route | ❌ 404 Not Found |

---

## 🚀 Automated Testing Script

Save this as `test-gateway.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "🧪 Testing Gateway Service..."
echo ""

# Test 1: Health Check
echo "1️⃣ Testing health endpoint..."
curl -s $BASE_URL/actuator/health | grep -q "UP" && echo "✅ Health check passed" || echo "❌ Health check failed"
echo ""

# Test 2: Register
echo "2️⃣ Testing user registration..."
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test'$(date +%s)'@test.com",
    "password": "SecurePass123",
    "role": "SELLER"
  }')

if echo "$REGISTER_RESPONSE" | grep -q "id"; then
    echo "✅ Registration passed"
else
    echo "❌ Registration failed"
fi
echo ""

# Test 3: Login
echo "3️⃣ Testing login..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@test.com",
    "password": "SecurePass123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo "✅ Login passed"
    echo "Token: ${TOKEN:0:20}..."
else
    echo "❌ Login failed"
fi
echo ""

# Test 4: Protected route without token
echo "4️⃣ Testing protected route without token..."
curl -s $BASE_URL/me | grep -q "Missing authorization" && echo "✅ Auth check passed" || echo "❌ Auth check failed"
echo ""

# Test 5: Protected route with token
echo "5️⃣ Testing protected route with token..."
if [ -n "$TOKEN" ]; then
    PROFILE_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/me)
    if echo "$PROFILE_RESPONSE" | grep -q "email"; then
        echo "✅ Protected route passed"
    else
        echo "❌ Protected route failed"
    fi
else
    echo "⏭️ Skipped (no token)"
fi
echo ""

echo "✨ Testing complete!"
```

Make it executable:
```bash
chmod +x test-gateway.sh
./test-gateway.sh
```

---

Good luck with testing! 🎯
