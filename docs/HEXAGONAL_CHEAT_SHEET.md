# Hexagonal Architecture - Quick Reference Cheat Sheet

## 🎯 The 3-Second Rule
**When creating ANY class, ask: "Which layer does this belong to?"**

---

## 📁 Layer Guide

| Layer | What Goes Here | Framework Dependencies? | Example |
|-------|---------------|------------------------|---------|
| **domain/** | Business logic, entities, rules | ❌ NO | `User.java`, `RegisterUserService.java` |
| **application/** | REST controllers, DTOs | ✅ YES (Spring Web) | `AuthController.java`, `RegisterRequest.java` |
| **infrastructure/** | Database, security, external APIs | ✅ YES (Spring Data, Security) | `UserRepositoryAdapter.java`, `MongoConfig.java` |
| **shared/** | Utilities, exception handlers | ✅ YES (Spring Web) | `GlobalExceptionHandler.java` |

---

## 🔌 Ports Quick Reference

### Inbound Ports (Use Cases) - `domain/port/in/`
**Purpose:** Define what users can DO

**Naming:** `[Action][Entity]UseCase`

**Example:**
```java
public interface RegisterUserUseCase {
    UserResponse register(RegisterCommand command);
}
```

### Outbound Ports (Dependencies) - `domain/port/out/`
**Purpose:** Define what the domain NEEDS

**Naming:** `[What][Where]Port`

**Example:**
```java
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(Email email);
}
```

---

## 🔄 Mappers Quick Reference

### 1. **DtoMapper** - `application/mapper/`
**Purpose:** Convert HTTP DTOs ↔ Domain objects

**Example:**
```java
public class UserDtoMapper {
    public static UserResponse toResponse(User user) { ... }
    public static RegisterCommand toCommand(RegisterRequest dto) { ... }
}
```

### 2. **EntityMapper** - `infrastructure/persistence/mapper/`
**Purpose:** Convert Database Entities ↔ Domain objects

**Example:**
```java
@Component
public class UserEntityMapper {
    public UserEntity toEntity(User user) { ... }
    public User toDomain(UserEntity entity) { ... }
}
```

**NEVER:** Mix these two mappers!

---

## 🚫 Common Mistakes

| ❌ Wrong | ✅ Right | Why |
|---------|---------|-----|
| `@Service` in domain service | No annotations in domain | Domain is framework-free |
| Controller calls repository directly | Controller → UseCase → Repository | Separation of concerns |
| Domain imports `UserEntity` | Domain uses `User` model | Domain doesn't know about DB |
| DTOs in domain layer | DTOs in application layer | DTOs are HTTP-specific |
| `UserRepositoryPort` in infrastructure | `UserRepositoryPort` in domain/port/out | Ports are domain contracts |

---

## 🏗️ Adding a New Feature Checklist

**Example: "Add ability for users to update email"**

### Step 1: Domain Layer
- [ ] Create use case interface: `UpdateUserEmailUseCase.java` (domain/port/in/)
- [ ] Implement service: `UpdateUserEmailService.java` (domain/service/)
- [ ] Add method to existing port: `UserRepositoryPort.findById()` (if needed)
- [ ] Create domain exception: `EmailAlreadyTakenException.java` (domain/exception/)

### Step 2: Application Layer
- [ ] Create DTO: `UpdateEmailRequest.java` (application/dto/request/)
- [ ] Create DTO: `UpdateEmailResponse.java` (application/dto/response/)
- [ ] Add mapper methods in `UserDtoMapper.java`
- [ ] Add endpoint in `UserController.java`

### Step 3: Infrastructure Layer
- [ ] Implement repository method in `UserRepositoryAdapter.java`
- [ ] Update security config if needed

### Step 4: Shared Layer
- [ ] Add exception handler in `GlobalExceptionHandler.java`

---

## 📊 Request Flow Diagram

```
HTTP Request (JSON)
        ↓
    Controller ──────► Validate & Convert to DTO
        ↓
    DtoMapper ──────► Convert DTO to Domain Command
        ↓
    Use Case  ──────► Execute Business Logic
        ↓
    Repository Port ──► Interface (domain contract)
        ↓
    Repository Adapter → Implementation (infrastructure)
        ↓
    EntityMapper ─────► Convert Domain to Entity
        ↓
    Spring Data Repo ─► Save to MongoDB
        ↓
    EntityMapper ─────► Convert Entity to Domain
        ↓
    DtoMapper ────────► Convert Domain to Response DTO
        ↓
    Controller ───────► Return HTTP Response
```

---

## 🧪 Testing Quick Guide

### Test Domain Logic (No Spring!)
```java
class RegisterUserServiceTest {
    
    @Test
    void shouldRegisterNewUser() {
        // Arrange - Mock ports
        UserRepositoryPort mockRepo = mock(UserRepositoryPort.class);
        PasswordEncoderPort mockEncoder = mock(PasswordEncoderPort.class);
        
        when(mockRepo.findByEmail(any())).thenReturn(Optional.empty());
        when(mockEncoder.encode(any())).thenReturn(new Password("hashed"));
        
        RegisterUserService service = new RegisterUserService(mockRepo, mockEncoder);
        RegisterCommand command = new RegisterCommand(...);
        
        // Act
        UserResponse response = service.register(command);
        
        // Assert
        assertNotNull(response);
        verify(mockRepo).save(any(User.class));
    }
}
```

### Test Controller (With Spring)
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RegisterUserUseCase registerUseCase;
    
    @Test
    void shouldRegisterUser() throws Exception {
        // Arrange
        when(registerUseCase.register(any())).thenReturn(new UserResponse(...));
        
        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "John",
                        "email": "john@example.com",
                        "password": "Pass123",
                        "role": "CLIENT"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John"));
    }
}
```

---

## 🎯 Dependency Direction Rule

```
application/     ──────►  domain/  ◄──────  infrastructure/
                                ▲
                                │
                         ONLY THIS WAY!
                         (dependencies point inward)
```

**Golden Rule:** Dependencies point INWARD toward the domain!

- ✅ `application/` can import from `domain/`
- ✅ `infrastructure/` can import from `domain/`
- ❌ `domain/` NEVER imports from `application/` or `infrastructure/`

---

## 📦 Package Naming Conventions

```
com.ecommerce.user
├── domain
│   ├── model                    # Entities, Value Objects
│   ├── port.in                  # Use cases
│   ├── port.out                 # Repository/Service interfaces
│   ├── service                  # Business logic implementations
│   └── exception                # Domain exceptions
│
├── application
│   ├── rest                     # Controllers
│   ├── dto.request              # Request DTOs
│   ├── dto.response             # Response DTOs
│   └── mapper                   # DTO mappers
│
├── infrastructure
│   ├── persistence.entity       # DB entities
│   ├── persistence.repository   # Spring Data + Adapters
│   ├── persistence.mapper       # Entity mappers
│   ├── security.adapter         # Security implementations
│   ├── security.config          # Spring Security config
│   └── config                   # General config
│
└── shared
    ├── exception                # Global exception handler
    └── util                     # Utilities
```

---

## 🔑 Key Vocabulary

| Term | Meaning | Example |
|------|---------|---------|
| **Port** | Interface/contract | `UserRepositoryPort` |
| **Adapter** | Implementation of a port | `UserRepositoryAdapter` |
| **Use Case** | What users can do | `RegisterUserUseCase` |
| **Domain Model** | Pure business entity | `User.java` (no @Document) |
| **Entity** | Database model | `UserEntity.java` (with @Document) |
| **DTO** | HTTP request/response | `RegisterRequest.java` |
| **Value Object** | Immutable object with validation | `Email.java`, `Password.java` |

---

## 💡 When in Doubt

**Ask yourself:**
1. Does this class contain **business rules**? → `domain/`
2. Does this class handle **HTTP requests**? → `application/`
3. Does this class talk to **database/external services**? → `infrastructure/`
4. Is this a **utility/helper**? → `shared/`

---

## 🚀 Pro Tips

1. **Start with domain** - Define your use cases first
2. **Keep domain pure** - No Spring, no MongoDB, just Java
3. **Use records for DTOs** - They're immutable and concise
4. **Test domain without Spring** - Faster tests, better design
5. **One adapter per port** - Don't mix responsibilities

---

## 📌 Remember

> "The domain is the heart of your application.  
> Everything else is just plumbing."

**Protect your domain from framework details!**
