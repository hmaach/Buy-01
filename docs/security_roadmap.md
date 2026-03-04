Goal:

- Only **User Service** can issue tokens (private key).
- **Gateway, Product, Media** can only validate tokens (public key).
- Services remain network-private.

---

# Phase 1 — Key Strategy

## 1️⃣ Generate RSA Key Pair

Generate a 2048-bit RSA key pair:

```bash
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
```

You now have:

- `private.pem` → **ONLY User Service**
- `public.pem` → Gateway + Product + Media

Do not commit `private.pem` to Git.

---

# Phase 2 — User Service (Authorization Server Lite)

User Service responsibilities:

- Authenticate user (email/password)
- Hash password with BCrypt
- Issue JWT signed with private key

Use Spring Security with Nimbus JWT encoder.

---

## 2️⃣ Add Dependencies

Spring Boot:

- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
- spring-security-oauth2-jose

---

## 3️⃣ Load Private Key

Example config:

```java
@Bean
JwtEncoder jwtEncoder() {
    RSAKey rsaKey = new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .build();
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
}
```

---

## 4️⃣ Generate Token

When login succeeds:

```java
Instant now = Instant.now();

JwtClaimsSet claims = JwtClaimsSet.builder()
    .issuer("user-service")
    .subject(user.getId())
    .claim("roles", user.getRoles())
    .issuedAt(now)
    .expiresAt(now.plus(1, ChronoUnit.HOURS))
    .build();

String token = jwtEncoder.encode(
    JwtEncoderParameters.from(claims)
).getTokenValue();
```

Token includes:

- `sub` → userId
- `roles` → CLIENT or SELLER
- `exp`
- `iss`

---

# Phase 3 — Gateway Configuration

Gateway validates JWT using **public key only**.

If using Spring Cloud Gateway:

### 5️⃣ Configure as Resource Server

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          public-key-location: classpath:public.pem
```

Security config:

```java
http
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/auth/**").permitAll()
      .anyRequest().authenticated()
  )
  .oauth2ResourceServer(oauth -> oauth.jwt());
```

Gateway:

- Rejects invalid tokens
- Forwards valid requests downstream
- Keeps Authorization header

---

# Phase 4 — Product & Media Services

These are pure **Resource Servers**.

They:

- Validate JWT
- Extract subject
- Enforce roles
- Enforce ownership

---

## 6️⃣ Configure Public Key

Same config as gateway:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          public-key-location: classpath:public.pem
```

---

## 7️⃣ Role Mapping

JWT contains:

```json
"roles": ["SELLER"]
```

You must convert roles into Spring authorities:

```java
@Bean
JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter rolesConverter =
        new JwtGrantedAuthoritiesConverter();
    rolesConverter.setAuthoritiesClaimName("roles");
    rolesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter converter =
        new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(rolesConverter);

    return converter;
}
```

---

# Phase 5 — Ownership Enforcement (Critical)

Never trust request payload sellerId.

Inside Product Service:

```java
String sellerId = jwt.getSubject();
```

Then verify:

```java
if (!product.getSellerId().equals(sellerId)) {
    throw new AccessDeniedException("Not owner");
}
```

This is more important than cryptography.

---

# Phase 6 — Docker Setup

Only expose gateway:

```yaml
gateway:
  ports:
    - "8080:8080"

product:
  expose:
    - "8081"

media:
  expose:
    - "8082"
```

Keep everything on internal network.

---

# Phase 7 — HTTPS (Optional for Local, Required in Production)

In production:

- Terminate HTTPS at reverse proxy (Nginx or cloud load balancer)
- Forward to gateway internally

You may use Let's Encrypt for certificates.

---

# Phase 8 — Token Design Best Practices

Use claims:

| Claim | Purpose         |
| ----- | --------------- |
| sub   | userId          |
| roles | CLIENT / SELLER |
| iss   | token issuer    |
| exp   | expiration      |
| iat   | issued time     |

Keep token small.
Do not store sensitive data inside.

---

# Final Architecture Summary

| Component    | Has Private Key | Has Public Key | Validates JWT |
| ------------ | --------------- | -------------- | ------------- |
| User Service | ✅              | ✅             | Optional      |
| Gateway      | ❌              | ✅             | ✅            |
| Product      | ❌              | ✅             | ✅            |
| Media        | ❌              | ✅             | ✅            |

Only User Service can generate tokens.
All others can only verify.

---

# Why This Is Cleaner

- No shared secrets
- No risk of token forging by other services
- Clear separation of responsibility
- Industry-aligned architecture

---

# Final Recommendation For You

For your e-commerce platform:

Use RS256.
Keep services private.
Validate JWT everywhere.
Enforce ownership strictly.
Do not add internal secret headers.

This is professional, secure, and still manageable for your level.

If you want, next I can provide a minimal working Spring Boot configuration for one full service (ready-to-copy structure).
