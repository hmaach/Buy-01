# User Service - Hexagonal Architecture Documentation

## Table of Contents

1. [What is Hexagonal Architecture?](#what-is-hexagonal-architecture)
2. [Project Structure Overview](#project-structure-overview)
3. [Layer-by-Layer Explanation](#layer-by-layer-explanation)
4. [Component Details](#component-details)
5. [Request Flow Example](#request-flow-example)
6. [Key Principles & Rules](#key-principles--rules)
7. [Quick Start Guide](#quick-start-guide)

---

## What is Hexagonal Architecture?

**Also known as:** Ports and Adapters Architecture

**Main Goal:** Isolate business logic from external frameworks (Spring, MongoDB, REST, etc.)

**Hexagonal Architecture = Plug & Play Architecture**

```
         Pluggable         Pluggable
          REST API        Database
             ↓               ↓
        ┌────────────────────────┐
        │   BUSINESS LOGIC       │
        │   (Domain Layer)       │
        │  - Pure Java           │
        │  - Framework-free      │
        └────────────────────────┘
             ↑               ↑
          Pluggable       Pluggable
          Messaging       Security
```

You can change any adapter (REST → GraphQL, MongoDB → PostgreSQL, BCrypt → Argon2) without touching the core business logic!

### Why Use It?

✅ **Testable** - Test business logic without database or web server  
✅ **Flexible** - Swap databases (MongoDB → PostgreSQL) without touching business logic  
✅ **Maintainable** - Clear separation makes code easier to understand  
✅ **Framework-independent** - Business rules don't depend on Spring or any framework

### The Hexagon Concept

```
        ┌─────────────────────────────────┐
        │                                 │
        │    REST API (Inbound)           │
        │    ┌─────────────────┐          │
        │    │   Controllers   │          │
        │    └────────┬────────┘          │
        │             │                   │
        │    ┌────────▼────────┐          │
        │    │                 │          │
        │    │  BUSINESS LOGIC │◄─────────┼──── Database (Outbound)
        │    │    (Domain)     │          │
        │    │                 │          │
        │    └────────┬────────┘          │
        │             │                   │
        │    ┌────────▼────────┐          │
        │    │   Kafka Event   │          │
        │    └─────────────────┘          │
        │                                 │
        └─────────────────────────────────┘
```

**Key Idea:** The hexagon (business logic) is in the center. Everything else is outside.

---

## Project Structure Overview

```
user-service/
├── domain/              ← CORE: Business logic (No Spring, No MongoDB!)
├── application/         ← INBOUND: REST API (Controllers, DTOs)
├── infrastructure/      ← OUTBOUND: Database, Security, External services
└── shared/              ← UTILITIES: Exception handlers, formatters
```

### Dependency Direction (IMPORTANT!)

```
application/     ──────►  domain/  ◄──────  infrastructure/
(Controllers)           (Business)         (Database, Security)
```

**Rule:** `domain/` NEVER imports from `application/` or `infrastructure/`

---

## Layer-by-Layer Explanation

### 1 **DOMAIN Layer** (The Heart ❤️)

**Location:** `src/main/java/com/ecommerce/user/domain/`

**Purpose:** Contains ALL business logic, rules, and entities.

**Key Rule:** NO framework dependencies! Only pure Java.

#### Components:

##### **model/** - Business Entities

- **User.java** - The core user entity (not a database model!)
- **Role.java** - Enum (CLIENT, SELLER)
- **valueobject/** - Special objects with validation
  - `Email.java` - Validates email format
  - `Password.java` - Validates password strength

**Example: User.java**

```java
public class User {
    private String id;
    private String name;
    private Email email;        // Value object
    private Password password;  // Value object
    private Role role;

    // Business logic methods
    public boolean isSeller() {
        return role == Role.SELLER;
    }
}
```

##### **port/in/** - Use Cases (What the service CAN DO)

Interfaces that define what actions users can perform:

- `RegisterUserUseCase.java` - "I want to register"
- `LoginUserUseCase.java` - "I want to login"
- `GetUserProfileUseCase.java` - "I want to see my profile"
- `UpdateUserProfileUseCase.java` - "I want to update my profile"

**Example:**

```java
public interface RegisterUserUseCase {
    UserResponse register(RegisterCommand command);
}
```

##### **port/out/** - Dependencies (What the service NEEDS)

Interfaces for things the domain needs but doesn't implement:

- `UserRepositoryPort.java` - "I need to save/find users"
- `PasswordEncoderPort.java` - "I need to hash passwords"
- `TokenGeneratorPort.java` - "I need to generate JWT tokens"
- `MediaServicePort.java` - "I need to upload avatar images"

**Example:**

```java
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(Email email);
    Optional<User> findById(String id);
}
```

##### **service/** - Business Logic Implementation

Classes that implement the use cases:

- `RegisterUserService.java` - Implements `RegisterUserUseCase`
- `LoginUserService.java` - Implements `LoginUserUseCase`

**Example: RegisterUserService.java**

```java
@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public UserResponse register(RegisterCommand command) {
        // 1. Check if user exists
        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        // 2. Create user with encoded password
        User user = new User(
            command.name(),
            command.email(),
            passwordEncoder.encode(command.password()),
            command.role()
        );

        // 3. Save and return
        User savedUser = userRepository.save(user);
        return UserDtoMapper.toResponse(savedUser);
    }
}
```

##### **exception/** - Domain Exceptions

Custom exceptions for business rule violations:

- `UserAlreadyExistsException` - Email already taken
- `InvalidCredentialsException` - Wrong password
- `UserNotFoundException` - User doesn't exist

---

### **APPLICATION Layer** (Inbound Adapters 📥)

**Location:** `src/main/java/com/ecommerce/user/application/`

**Purpose:** Handle HTTP requests and convert them to domain operations.

#### Components:

##### **rest/** - REST Controllers

Expose endpoints to the outside world:

- `AuthController.java` - `/auth/register`, `/auth/login`
- `UserController.java` - `/me` (get/update profile)

**Example: AuthController.java**

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUseCase;
    private final LoginUserUseCase loginUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        RegisterCommand command = UserDtoMapper.toCommand(request);
        UserResponse response = registerUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginCommand command = UserDtoMapper.toCommand(request);
        LoginResponse response = loginUseCase.login(command);
        return ResponseEntity.ok(response);
    }
}
```

##### **dto/** - Data Transfer Objects

Objects for HTTP request/response:

**request/**

- `RegisterRequest.java` - JSON for registration
- `LoginRequest.java` - JSON for login
- `UpdateProfileRequest.java` - JSON for profile update

**response/**

- `UserResponse.java` - User data to return
- `LoginResponse.java` - Token + user data

**Example: RegisterRequest.java**

```java
public record RegisterRequest(
    @NotBlank String name,
    @Email String email,
    @NotBlank String password,
    @NotNull Role role
) {}
```

##### **mapper/** - DTO ↔ Domain Conversion

Converts between DTOs and Domain objects:

- `UserDtoMapper.java`

**Example:**

```java
public class UserDtoMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail().getValue(),
            user.getRole()
        );
    }

    public static RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(
            request.name(),
            new Email(request.email()),
            new Password(request.password()),
            request.role()
        );
    }
}
```

---

### **INFRASTRUCTURE Layer** (Outbound Adapters)

**Location:** `src/main/java/com/ecommerce/user/infrastructure/`

**Purpose:** Implement technical details (database, security, external services).

#### Components:

##### **persistence/** - Database Adapter

**entity/** - MongoDB documents

- `UserEntity.java` - The actual MongoDB document (annotated with `@Document`)

```java
@Document(collection = "users")
public class UserEntity {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;  // Hashed
    private Role role;
    // getters/setters
}
```

**repository/** - Database operations

- `MongoUserRepository.java` - Spring Data MongoDB interface
- `UserRepositoryAdapter.java` - Implements `UserRepositoryPort`

```java
// Spring Data interface
public interface MongoUserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
}

// Adapter that implements domain port
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final MongoUserRepository mongoRepository;
    private final UserEntityMapper mapper;

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = mongoRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return mongoRepository.findByEmail(email.getValue())
            .map(mapper::toDomain);
    }
}
```

**mapper/** - Entity ↔ Domain conversion

- `UserEntityMapper.java`

```java
@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail().getValue());
        entity.setPassword(user.getPassword().getValue());
        entity.setRole(user.getRole());
        return entity;
    }

    public User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getName(),
            new Email(entity.getEmail()),
            new Password(entity.getPassword()),
            entity.getRole()
        );
    }
}
```

##### **security/** - Security Adapters

**adapter/** - Security implementations

- `BcryptPasswordEncoderAdapter.java` - Implements `PasswordEncoderPort`
- `JwtTokenGeneratorAdapter.java` - Implements `TokenGeneratorPort`

```java
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public Password encode(Password password) {
        String hashed = encoder.encode(password.getValue());
        return new Password(hashed);
    }

    @Override
    public boolean matches(Password raw, Password encoded) {
        return encoder.matches(raw.getValue(), encoded.getValue());
    }
}
```

**config/** - Security configuration

- `SecurityConfig.java` - Spring Security setup

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/me").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}
```

##### **config/** - General Configuration

- `BeanConfiguration.java` - Wire all adapters as Spring beans
- `MongoConfig.java` - MongoDB connection settings

```java
@Configuration
public class BeanConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
        UserRepositoryPort userRepository,
        PasswordEncoderPort passwordEncoder
    ) {
        return new RegisterUserService(userRepository, passwordEncoder);
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(
        UserRepositoryPort userRepository,
        PasswordEncoderPort passwordEncoder,
        TokenGeneratorPort tokenGenerator
    ) {
        return new LoginUserService(userRepository, passwordEncoder, tokenGenerator);
    }
}
```

---

### **SHARED Layer** (Cross-Cutting)

**Location:** `src/main/java/com/ecommerce/user/shared/`

**Purpose:** Utilities used across all layers.

##### **exception/** - Global Exception Handler

- `GlobalExceptionHandler.java` - Catch exceptions and return proper HTTP responses

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("User already exists"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Invalid email or password"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("User not found"));
    }
}
```

##### **util/** - Utilities

- `Format.java` - String formatting, validation helpers

---

## Request Flow Example

Let's trace a **user registration** request from start to finish:

```
1. HTTP Request arrives
   POST /auth/register
   {
     "name": "John Doe",
     "email": "john@example.com",
     "password": "SecurePass123",
     "role": "SELLER"
   }

2. AuthController receives request
   ├─ Validates JSON format
   ├─ Converts RegisterRequest → RegisterCommand
   └─ Calls registerUseCase.register(command)

3. RegisterUserService (domain layer)
   ├─ Checks if email exists via UserRepositoryPort
   ├─ Creates User domain object
   ├─ Encodes password via PasswordEncoderPort
   ├─ Saves user via UserRepositoryPort
   └─ Returns UserResponse

4. UserRepositoryAdapter (infrastructure layer)
   ├─ Converts User → UserEntity
   ├─ Saves to MongoDB via MongoUserRepository
   ├─ Converts UserEntity → User
   └─ Returns to service

5. AuthController
   ├─ Receives UserResponse
   └─ Returns HTTP 201 with user data
```

### Visual Flow

```
HTTP Request
    ↓
AuthController (application)
    ↓
RegisterUserService (domain)
    ↓
UserRepositoryPort (domain interface)
    ↓
UserRepositoryAdapter (infrastructure)
    ↓
MongoUserRepository (Spring Data)
    ↓
MongoDB Database
```

---

## Key Principles & Rules

### 1. **Dependency Rule**

```
Domain knows nothing about:
  ❌ Spring annotations (@Service, @Autowired)
  ❌ MongoDB (@Document, @Id)
  ❌ HTTP (@RestController, @PostMapping)
  ❌ Any framework code

Domain only uses:
  ✅ Pure Java classes
  ✅ Interfaces (ports)
  ✅ Business logic
```

### 2. **Port Naming Convention**

- **Inbound ports (use cases):** `RegisterUserUseCase`, `LoginUserUseCase`
- **Outbound ports:** `UserRepositoryPort`, `PasswordEncoderPort`

### 3. **Mapper Separation**

- **UserDtoMapper** - Converts DTO ↔ Domain
- **UserEntityMapper** - Converts Entity ↔ Domain

Never mix these! DTOs and Entities serve different purposes.

### 4. **Exception Handling**

- Domain throws **domain exceptions** (`UserNotFoundException`)
- `GlobalExceptionHandler` catches them and returns proper HTTP responses

---

## Quick Start Guide

### Step 1: Understand the Layers

1. **domain/** = Business rules (the "what")
2. **application/** = REST API (the "how we talk to users")
3. **infrastructure/** = Technical stuff (the "how we store/secure data")

### Step 2: Follow the Flow

When adding a new feature:

1. Create the **use case interface** in `domain/port/in/`
2. Create the **service implementation** in `domain/service/`
3. Create the **controller** in `application/rest/`
4. Create **DTOs** in `application/dto/`
5. Create **adapters** in `infrastructure/` if needed

### Step 3: Remember the Rules

- Domain layer is **pure Java** (no frameworks)
- Controllers **never** access repositories directly
- Always go through **use cases** (ports)

---

## Additional Resources

### Hexagonal Architecture

- [Hexagonal Architecture Explained](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

### Spring Boot with Hexagonal

- [Building Hexagonal Architecture with Spring Boot](https://reflectoring.io/spring-hexagonal/)
- [Ports and Adapters Pattern](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)
