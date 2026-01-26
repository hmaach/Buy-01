# Buy01 E-Commerce Platform - Complete Project Structure

## 📁 Root Directory Structure

```
buy01-platform/
│
├── .github/                                    # CI/CD workflows
│   └── workflows/
│       ├── backend-ci.yml                      # Backend build/test
│       ├── frontend-ci.yml                     # Angular build/test
│       └── deploy.yml                          # Deployment pipeline
│
├── docs/                                       # Documentation
│   ├── architecture/
│   │   ├── C4-diagrams.md                      # System architecture diagrams
│   │   ├── database-schema.md                  # MongoDB collections design
│   │   ├── api-contracts.md                    # Service API specifications
│   │   └── event-catalog.md                    # Kafka events documentation
│   ├── adr/                                    # Architecture Decision Records
│   │   ├── 001-microservices-architecture.md
│   │   ├── 002-database-per-service.md
│   │   ├── 003-jwt-authentication.md
│   │   └── 004-event-driven-communication.md
│   ├── deployment/
│   │   ├── docker-setup.md
│   │   ├── kubernetes-setup.md
│   │   └── production-checklist.md
│   └── development/
│       ├── getting-started.md
│       ├── coding-standards.md
│       └── testing-guide.md
│
├── scripts/                                    # Utility scripts
│   ├── start-dev.sh                            # Start all services locally
│   ├── stop-dev.sh                             # Stop all services
│   ├── clean-docker.sh                         # Clean Docker volumes
│   ├── init-db.sh                              # Initialize databases
│   ├── generate-jwt-secret.sh                  # Generate JWT secret
│   └── health-check.sh                         # Check all services health
│
├── infrastructure/                             # Infrastructure as Code
│   ├── terraform/                              # Terraform configs
│   │   ├── modules/
│   │   │   ├── mongodb/
│   │   │   ├── kafka/
│   │   │   └── k8s-cluster/
│   │   ├── environments/
│   │   │   ├── dev/
│   │   │   ├── staging/
│   │   │   └── prod/
│   │   └── main.tf
│   ├── kubernetes/                             # K8s manifests
│   │   ├── base/
│   │   │   ├── namespace.yaml
│   │   │   ├── configmap.yaml
│   │   │   └── secrets.yaml
│   │   ├── services/
│   │   │   ├── discovery-service/
│   │   │   ├── api-gateway/
│   │   │   ├── user-service/
│   │   │   ├── product-service/
│   │   │   └── media-service/
│   │   └── infrastructure/
│   │       ├── mongodb.yaml
│   │       ├── kafka.yaml
│   │       └── minio.yaml
│   └── helm/                                   # Helm charts
│       └── buy01/
│           ├── Chart.yaml
│           ├── values.yaml
│           ├── values-dev.yaml
│           ├── values-prod.yaml
│           └── templates/
│
├── monitoring/                                 # Observability stack
│   ├── prometheus/
│   │   ├── prometheus.yml
│   │   └── alert-rules.yml
│   ├── grafana/
│   │   ├── dashboards/
│   │   │   ├── service-metrics.json
│   │   │   ├── kafka-metrics.json
│   │   │   └── business-metrics.json
│   │   └── provisioning/
│   └── elk/
│       ├── logstash/
│       │   └── pipeline.conf
│       └── elasticsearch/
│           └── index-template.json
│
├── backend/                                    # All backend services
│   │
│   ├── discovery-service/                      # Eureka Server (Port 8761)
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/buy01/discovery/
│   │   │   │   │   ├── DiscoveryServiceApplication.java
│   │   │   │   │   └── config/
│   │   │   │   │       └── SecurityConfig.java
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       ├── application-docker.yml
│   │   │   │       └── application-prod.yml
│   │   │   └── test/
│   │   │       └── java/com/buy01/discovery/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   ├── .dockerignore
│   │   └── README.md
│   │
│   ├── config-service/                         # Spring Cloud Config Server (Optional - Port 8888)
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/buy01/config/
│   │   │   │   │   ├── ConfigServiceApplication.java
│   │   │   │   │   └── config/
│   │   │   │   │       └── SecurityConfig.java
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       └── bootstrap.yml
│   │   │   └── test/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── README.md
│   │
│   ├── api-gateway/                            # Spring Cloud Gateway (Port 8080)
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/buy01/gateway/
│   │   │   │   │   ├── ApiGatewayApplication.java
│   │   │   │   │   ├── config/
│   │   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   │   └── GatewayConfig.java
│   │   │   │   │   ├── filter/
│   │   │   │   │   │   ├── AuthenticationFilter.java
│   │   │   │   │   │   ├── LoggingFilter.java
│   │   │   │   │   │   └── RateLimitFilter.java
│   │   │   │   │   ├── security/
│   │   │   │   │   │   ├── JwtConverter.java
│   │   │   │   │   │   └── JwtUtil.java
│   │   │   │   │   └── exception/
│   │   │   │   │       ├── GlobalErrorHandler.java
│   │   │   │   │       └── GatewayException.java
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       ├── application-docker.yml
│   │   │   │       └── application-prod.yml
│   │   │   └── test/
│   │   │       └── java/com/buy01/gateway/
│   │   │           ├── filter/
│   │   │           └── integration/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── README.md
│   │
│   ├── user-service/                           # User & Authentication Service (Port 8081)
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/buy01/user/
│   │   │   │   │   ├── UserServiceApplication.java
│   │   │   │   │   │
│   │   │   │   │   ├── domain/                 # Hexagonal - Core Domain
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   │   ├── Role.java (enum: CLIENT, SELLER, ADMIN)
│   │   │   │   │   │   │   ├── UserId.java (Value Object)
│   │   │   │   │   │   │   ├── Email.java (Value Object)
│   │   │   │   │   │   │   └── Password.java (Value Object)
│   │   │   │   │   │   ├── event/
│   │   │   │   │   │   │   ├── UserRegisteredEvent.java
│   │   │   │   │   │   │   ├── UserUpdatedEvent.java
│   │   │   │   │   │   │   └── UserDeletedEvent.java
│   │   │   │   │   │   └── exception/
│   │   │   │   │   │       ├── UserNotFoundException.java
│   │   │   │   │   │       ├── InvalidCredentialsException.java
│   │   │   │   │   │       └── EmailAlreadyExistsException.java
│   │   │   │   │   │
│   │   │   │   │   ├── application/            # Use Cases
│   │   │   │   │   │   ├── port/
│   │   │   │   │   │   │   ├── in/             # Inbound Ports (interfaces)
│   │   │   │   │   │   │   │   ├── RegisterUserUseCase.java
│   │   │   │   │   │   │   │   ├── LoginUserUseCase.java
│   │   │   │   │   │   │   │   ├── GetUserProfileUseCase.java
│   │   │   │   │   │   │   │   ├── UpdateUserProfileUseCase.java
│   │   │   │   │   │   │   │   └── DeleteUserUseCase.java
│   │   │   │   │   │   │   └── out/            # Outbound Ports (interfaces)
│   │   │   │   │   │   │       ├── UserRepository.java
│   │   │   │   │   │   │       ├── PasswordEncoder.java
│   │   │   │   │   │   │       ├── TokenGenerator.java
│   │   │   │   │   │   │       ├── EventPublisher.java
│   │   │   │   │   │   │       └── MediaServiceClient.java (Feign)
│   │   │   │   │   │   │
│   │   │   │   │   │   └── service/            # Use Case Implementations
│   │   │   │   │   │       ├── RegisterUserService.java
│   │   │   │   │   │       ├── LoginUserService.java
│   │   │   │   │   │       ├── UserProfileService.java
│   │   │   │   │   │       └── UserManagementService.java
│   │   │   │   │   │
│   │   │   │   │   ├── adapter/                # Adapters (Hexagonal)
│   │   │   │   │   │   ├── in/                 # Inbound Adapters
│   │   │   │   │   │   │   ├── web/            # REST Controllers
│   │   │   │   │   │   │   │   ├── AuthController.java
│   │   │   │   │   │   │   │   ├── UserController.java
│   │   │   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   │   │   ├── request/
│   │   │   │   │   │   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   │   │   │   │   └── UpdateProfileRequest.java
│   │   │   │   │   │   │   │   │   └── response/
│   │   │   │   │   │   │   │   │       ├── UserResponse.java
│   │   │   │   │   │   │   │   │       ├── TokenResponse.java
│   │   │   │   │   │   │   │   │       └── ProfileResponse.java
│   │   │   │   │   │   │   │   └── mapper/
│   │   │   │   │   │   │   │       └── UserDtoMapper.java
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   └── messaging/      # Kafka Consumers
│   │   │   │   │   │   │       └── UserEventConsumer.java
│   │   │   │   │   │   │
│   │   │   │   │   │   └── out/                # Outbound Adapters
│   │   │   │   │   │       ├── persistence/    # Database
│   │   │   │   │   │       │   ├── MongoUserRepository.java
│   │   │   │   │   │       │   ├── entity/
│   │   │   │   │   │       │   │   └── UserDocument.java
│   │   │   │   │   │       │   └── mapper/
│   │   │   │   │   │       │       └── UserEntityMapper.java
│   │   │   │   │   │       │
│   │   │   │   │   │       ├── security/       # Security implementations
│   │   │   │   │   │       │   ├── BCryptPasswordEncoder.java
│   │   │   │   │   │       │   └── JwtTokenGenerator.java
│   │   │   │   │   │       │
│   │   │   │   │   │       ├── messaging/      # Kafka Producers
│   │   │   │   │   │       │   └── KafkaEventPublisher.java
│   │   │   │   │   │       │
│   │   │   │   │   │       └── client/         # External service clients
│   │   │   │   │   │           └── MediaServiceFeignClient.java
│   │   │   │   │   │
│   │   │   │   │   └── config/                 # Configuration
│   │   │   │   │       ├── MongoConfig.java
│   │   │   │   │       ├── SecurityConfig.java
│   │   │   │   │       ├── KafkaConfig.java
│   │   │   │   │       ├── FeignConfig.java
│   │   │   │   │       ├── SwaggerConfig.java
│   │   │   │   │       └── GlobalExceptionHandler.java
│   │   │   │   │
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       ├── application-docker.yml
│   │   │   │       ├── application-prod.yml
│   │   │   │       └── db/
│   │   │   │           └── migration/          # MongoDB migrations (optional)
│   │   │   │
│   │   │   └── test/
│   │   │       └── java/com/buy01/user/
│   │   │           ├── domain/                 # Unit tests (domain logic)
│   │   │           │   └── model/
│   │   │           ├── application/            # Use case tests
│   │   │           │   └── service/
│   │   │           ├── adapter/
│   │   │           │   ├── web/                # Integration tests (API)
│   │   │           │   └── persistence/        # Repository tests
│   │   │           └── integration/            # End-to-end tests
│   │   │               └── UserServiceIntegrationTest.java
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   ├── .dockerignore
│   │   └── README.md
│   │
│   ├── product-service/                        # Product Catalog Service (Port 8082)
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/buy01/product/
│   │   │   │   │   ├── ProductServiceApplication.java
│   │   │   │   │   │
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── Product.java
│   │   │   │   │   │   │   ├── ProductId.java (Value Object)
│   │   │   │   │   │   │   ├── Price.java (Value Object)
│   │   │   │   │   │   │   ├── Category.java (enum)
│   │   │   │   │   │   │   ├── ProductStatus.java (enum: DRAFT, PUBLISHED, ARCHIVED)
│   │   │   │   │   │   │   └── ImageReference.java
│   │   │   │   │   │   ├── event/
│   │   │   │   │   │   │   ├── ProductCreatedEvent.java
│   │   │   │   │   │   │   ├── ProductUpdatedEvent.java
│   │   │   │   │   │   │   └── ProductDeletedEvent.java
│   │   │   │   │   │   └── exception/
│   │   │   │   │   │       ├── ProductNotFoundException.java
│   │   │   │   │   │       ├── UnauthorizedProductAccessException.java
│   │   │   │   │   │       └── InvalidPriceException.java
│   │   │   │   │   │
│   │   │   │   │   ├── application/
│   │   │   │   │   │   ├── port/
│   │   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   │   ├── CreateProductUseCase.java
│   │   │   │   │   │   │   │   ├── UpdateProductUseCase.java
│   │   │   │   │   │   │   │   ├── DeleteProductUseCase.java
│   │   │   │   │   │   │   │   ├── GetProductUseCase.java
│   │   │   │   │   │   │   │   ├── ListProductsUseCase.java
│   │   │   │   │   │   │   │   └── AttachImageUseCase.java
│   │   │   │   │   │   │   └── out/
│   │   │   │   │   │   │       ├── ProductRepository.java
│   │   │   │   │   │   │       ├── EventPublisher.java
│   │   │   │   │   │   │       ├── MediaServiceClient.java
│   │   │   │   │   │   │       └── UserServiceClient.java
│   │   │   │   │   │   │
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       ├── ProductManagementService.java
│   │   │   │   │   │       ├── ProductQueryService.java
│   │   │   │   │   │       └── ImageAttachmentService.java
│   │   │   │   │   │
│   │   │   │   │   ├── adapter/
│   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   ├── web/
│   │   │   │   │   │   │   │   ├── ProductController.java
│   │   │   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   │   │   ├── request/
│   │   │   │   │   │   │   │   │   │   ├── CreateProductRequest.java
│   │   │   │   │   │   │   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   │   │   │   │   │   │   └── AttachImageRequest.java
│   │   │   │   │   │   │   │   │   └── response/
│   │   │   │   │   │   │   │   │       ├── ProductResponse.java
│   │   │   │   │   │   │   │   │       └── ProductListResponse.java
│   │   │   │   │   │   │   │   └── mapper/
│   │   │   │   │   │   │   │       └── ProductDtoMapper.java
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   └── messaging/
│   │   │   │   │   │   │       ├── ProductEventConsumer.java
│   │   │   │   │   │   │       └── ImageEventConsumer.java
│   │   │   │   │   │   │
│   │   │   │   │   │   └── out/
│   │   │   │   │   │       ├── persistence/
│   │   │   │   │   │       │   ├── MongoProductRepository.java
│   │   │   │   │   │       │   ├── entity/
│   │   │   │   │   │       │   │   └── ProductDocument.java
│   │   │   │   │   │       │   └── mapper/
│   │   │   │   │   │       │       └── ProductEntityMapper.java
│   │   │   │   │   │       │
│   │   │   │   │   │       ├── messaging/
│   │   │   │   │   │       │   └── KafkaEventPublisher.java
│   │   │   │   │   │       │
│   │   │   │   │   │       └── client/
│   │   │   │   │   │           ├── MediaServiceFeignClient.java
│   │   │   │   │   │           └── UserServiceFeignClient.java
│   │   │   │   │   │
│   │   │   │   │   └── config/
│   │   │   │   │       ├── MongoConfig.java
│   │   │   │   │       ├── SecurityConfig.java
│   │   │   │   │       ├── KafkaConfig.java
│   │   │   │   │       ├── FeignConfig.java
│   │   │   │   │       ├── SwaggerConfig.java
│   │   │   │   │       └── GlobalExceptionHandler.java
│   │   │   │   │
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       ├── application-docker.yml
│   │   │   │       └── application-prod.yml
│   │   │   │
│   │   │   └── test/
│   │   │       └── java/com/buy01/product/
│   │   │           ├── domain/
│   │   │           ├── application/
│   │   │           ├── adapter/
│   │   │           └── integration/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── README.md
│   │
│   ├── media-service/                          # Media & File Upload Service (Port 8083)
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/buy01/media/
│   │   │   │   │   ├── MediaServiceApplication.java
│   │   │   │   │   │
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── MediaFile.java
│   │   │   │   │   │   │   ├── MediaId.java (Value Object)
│   │   │   │   │   │   │   ├── MediaType.java (enum: IMAGE, VIDEO, DOCUMENT)
│   │   │   │   │   │   │   ├── FileMetadata.java
│   │   │   │   │   │   │   └── StorageLocation.java
│   │   │   │   │   │   ├── event/
│   │   │   │   │   │   │   ├── MediaUploadedEvent.java
│   │   │   │   │   │   │   └── MediaDeletedEvent.java
│   │   │   │   │   │   └── exception/
│   │   │   │   │   │       ├── InvalidFileTypeException.java
│   │   │   │   │   │       ├── FileSizeExceededException.java
│   │   │   │   │   │       └── MediaNotFoundException.java
│   │   │   │   │   │
│   │   │   │   │   ├── application/
│   │   │   │   │   │   ├── port/
│   │   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   │   ├── UploadMediaUseCase.java
│   │   │   │   │   │   │   │   ├── DeleteMediaUseCase.java
│   │   │   │   │   │   │   │   ├── GetMediaUseCase.java
│   │   │   │   │   │   │   │   └── ListUserMediaUseCase.java
│   │   │   │   │   │   │   └── out/
│   │   │   │   │   │   │       ├── MediaRepository.java
│   │   │   │   │   │   │       ├── FileStorage.java (S3/MinIO)
│   │   │   │   │   │   │       ├── FileValidator.java
│   │   │   │   │   │   │       └── EventPublisher.java
│   │   │   │   │   │   │
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       ├── MediaUploadService.java
│   │   │   │   │   │       ├── MediaQueryService.java
│   │   │   │   │   │       ├── ImageProcessingService.java (thumbnails)
│   │   │   │   │   │       └── FileValidationService.java
│   │   │   │   │   │
│   │   │   │   │   ├── adapter/
│   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   ├── web/
│   │   │   │   │   │   │   │   ├── MediaController.java
│   │   │   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   │   │   ├── request/
│   │   │   │   │   │   │   │   │   │   └── UploadMediaRequest.java
│   │   │   │   │   │   │   │   │   └── response/
│   │   │   │   │   │   │   │   │       ├── MediaResponse.java
│   │   │   │   │   │   │   │   │       └── MediaListResponse.java
│   │   │   │   │   │   │   │   └── mapper/
│   │   │   │   │   │   │   │       └── MediaDtoMapper.java
│   │   │   │   │   │   │   │
│   │   │   │   │   │   │   └── messaging/
│   │   │   │   │   │   │       └── MediaEventConsumer.java
│   │   │   │   │   │   │
│   │   │   │   │   │   └── out/
│   │   │   │   │   │       ├── persistence/
│   │   │   │   │   │       │   ├── MongoMediaRepository.java
│   │   │   │   │   │       │   ├── entity/
│   │   │   │   │   │       │   │   └── MediaDocument.java
│   │   │   │   │   │       │   └── mapper/
│   │   │   │   │   │       │       └── MediaEntityMapper.java
│   │   │   │   │   │       │
│   │   │   │   │   │       ├── storage/
│   │   │   │   │   │       │   ├── S3FileStorage.java (or MinIOFileStorage)
│   │   │   │   │   │       │   └── LocalFileStorage.java (dev fallback)
│   │   │   │   │   │       │
│   │   │   │   │   │       ├── validation/
│   │   │   │   │   │       │   ├── MimeTypeValidator.java
│   │   │   │   │   │       │   └── FileSizeValidator.java
│   │   │   │   │   │       │
│   │   │   │   │   │       └── messaging/
│   │   │   │   │   │           └── KafkaEventPublisher.java
│   │   │   │   │   │
│   │   │   │   │   └── config/
│   │   │   │   │       ├── MongoConfig.java
│   │   │   │   │       ├── SecurityConfig.java
│   │   │   │   │       ├── S3Config.java
│   │   │   │   │       ├── KafkaConfig.java
│   │   │   │   │       ├── SwaggerConfig.java
│   │   │   │   │       └── GlobalExceptionHandler.java
│   │   │   │   │
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       ├── application-docker.yml
│   │   │   │       └── application-prod.yml
│   │   │   │
│   │   │   └── test/
│   │   │       └── java/com/buy01/media/
│   │   │           ├── domain/
│   │   │           ├── application/
│   │   │           ├── adapter/
│   │   │           └── integration/
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── README.md
│   │
│   └── shared/                                 # Shared libraries/modules
│       ├── common-domain/                      # Shared domain models
│       │   ├── src/main/java/com/buy01/common/
│       │   │   ├── event/
│       │   │   │   ├── DomainEvent.java
│       │   │   │   └── EventMetadata.java
│       │   │   ├── exception/
│       │   │   │   ├── BusinessException.java
│       │   │   │   └── TechnicalException.java
│       │   │   └── valueobject/
│       │   │       ├── Money.java
│       │   │       └── Timestamp.java
│       │   └── pom.xml
│       │
│       └── common-security/                    # Shared security utilities
│           ├── src/main/java/com/buy01/security/
│           │   ├── JwtUtil.java
│           │   ├── SecurityContextUtil.java
│           │   └── annotation/
│           │       ├── RequiresSeller.java
│           │       └── RequiresAuthentication.java
│           └── pom.xml
│
├── frontend/                                   # Angular Application
│   ├── buy01-web/
│   │   ├── src/
│   │   │   ├── app/
│   │   │   │   ├── core/                       # Singleton services, guards, interceptors
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── guards/
│   │   │   │   │   │   │   ├── auth.guard.ts
│   │   │   │   │   │   │   ├── role.guard.ts
│   │   │   │   │   │   │   ├── seller.guard.ts
│   │   │   │   │   │   │   └── client.guard.ts
│   │   │   │   │   │   ├── interceptors/
│   │   │   │   │   │   │   ├── auth.interceptor.ts
│   │   │   │   │   │   │   ├── error.interceptor.ts
│   │   │   │   │   │   │   └── loading.interceptor.ts
│   │   │   │   │   │   └── services/
│   │   │   │   │   │       ├── auth.service.ts
│   │   │   │   │   │       ├── token.service.ts
│   │   │   │   │   │       └── user.service.ts
│   │   │   │   │   │
│   │   │   │   │   ├── services/
│   │   │   │   │   │   ├── api.service.ts
│   │   │   │   │   │   ├── error-handler.service.ts
│   │   │   │   │   │   ├── notification.service.ts
│   │   │   │   │   │   └── loading.service.ts
│   │   │   │   │   │
│   │   │   │   │   └── models/
│   │   │   │   │       ├── user.model.ts
│   │   │   │   │       ├── auth.model.ts
│   │   │   │   │       └── api-response.model.ts
│   │   │   │   │
│   │   │   │   ├── shared/                     # Shared components, directives, pipes
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── navbar/
│   │   │   │   │   │   │   ├── navbar.component.ts
│   │   │   │   │   │   │   ├── navbar.component.html
│   │   │   │   │   │   │   └── navbar.component.scss
│   │   │   │   │   │   ├── footer/
│   │   │   │   │   │   ├── loading-spinner/
│   │   │   │   │   │   ├── error-message/
│   │   │   │   │   │   └── confirm-dialog/
│   │   │   │   │   │
│   │   │   │   │   ├── directives/
│   │   │   │   │   │   ├── image-fallback.directive.ts
│   │   │   │   │   │   └── debounce-click.directive.ts
│   │   │   │   │   │
│   │   │   │   │   ├── pipes/
│   │   │   │   │   │   ├── currency-format.pipe.ts
│   │   │   │   │   │   ├── date-ago.pipe.ts
│   │   │   │   │   │   └── file-size.pipe.ts
│   │   │   │   │   │
│   │   │   │   │   └── shared.module.ts
│   │   │   │   │
│   │   │   │   ├── features/                   # Feature modules (lazy loaded)
│   │   │   │   │   │
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   │   ├── login/
│   │   │   │   │   │   │   │   ├── login.component.ts
│   │   │   │   │   │   │   │   ├── login.component.html
│   │   │   │   │   │   │   │   └── login.component.scss
│   │   │   │   │   │   │   ├── register/
│   │   │   │   │   │   │   │   ├── register.component.ts
│   │   │   │   │   │   │   │   ├── register.component.html
│   │   │   │   │   │   │   │   └── register.component.scss
│   │   │   │   │   │   │   └── forgot-password/
│   │   │   │   │   │   ├── auth-routing.module.ts
│   │   │   │   │   │   └── auth.module.ts
│   │   │   │   │   │
│   │   │   │   │   ├── products/               # Public product browsing
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   │   ├── product-list/
│   │   │   │   │   │   │   │   ├── product-list.component.ts
│   │   │   │   │   │   │   │   ├── product-list.component.html
│   │   │   │   │   │   │   │   └── product-list.component.scss
│   │   │   │   │   │   │   ├── product-detail/
│   │   │   │   │   │   │   │   ├── product-detail.component.ts
│   │   │   │   │   │   │   │   ├── product-detail.component.html
│   │   │   │   │   │   │   │   └── product-detail.component.scss
│   │   │   │   │   │   │   ├── product-card/
│   │   │   │   │   │   │   └── product-filters/
│   │   │   │   │   │   ├── services/
│   │   │   │   │   │   │   └── product.service.ts
│   │   │   │   │   │   ├── models/
│   │   │   │   │   │   │   └── product.model.ts
│   │   │   │   │   │   ├── products-routing.module.ts
│   │   │   │   │   │   └── products.module.ts
│   │   │   │   │   │
│   │   │   │   │   ├── seller-dashboard/       # Seller-only features
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   │   ├── dashboard-home/
│   │   │   │   │   │   │   ├── manage-products/
│   │   │   │   │   │   │   │   ├── manage-products.component.ts
│   │   │   │   │   │   │   │   ├── manage-products.component.html
│   │   │   │   │   │   │   │   └── manage-products.component.scss
│   │   │   │   │   │   │   ├── create-product/
│   │   │   │   │   │   │   │   ├── create-product.component.ts
│   │   │   │   │   │   │   │   ├── create-product.component.html
│   │   │   │   │   │   │   │   └── create-product.component.scss
│   │   │   │   │   │   │   ├── edit-product/
│   │   │   │   │   │   │   └── media-manager/
│   │   │   │   │   │   │       ├── media-manager.component.ts
│   │   │   │   │   │   │       ├── media-manager.component.html
│   │   │   │   │   │   │       └── media-manager.component.scss
│   │   │   │   │   │   ├── services/
│   │   │   │   │   │   │   ├── seller-product.service.ts
│   │   │   │   │   │   │   └── media.service.ts
│   │   │   │   │   │   ├── seller-dashboard-routing.module.ts
│   │   │   │   │   │   └── seller-dashboard.module.ts
│   │   │   │   │   │
│   │   │   │   │   ├── user-profile/
│   │   │   │   │   │   ├── components/
│   │   │   │   │   │   │   ├── profile/
│   │   │   │   │   │   │   └── edit-profile/
│   │   │   │   │   │   ├── user-profile-routing.module.ts
│   │   │   │   │   │   └── user-profile.module.ts
│   │   │   │   │   │
│   │   │   │   │   └── media/                  # Media upload/management
│   │   │   │   │       ├── components/
│   │   │   │   │       │   ├── upload/
│   │   │   │   │       │   │   ├── upload.component.ts
│   │   │   │   │       │   │   ├── upload.component.html
│   │   │   │   │       │   │   └── upload.component.scss
│   │   │   │   │       │   ├── image-preview/
│   │   │   │   │       │   └── media-gallery/
│   │   │   │   │       ├── services/
│   │   │   │   │       │   └── upload.service.ts
│   │   │   │   │       ├── media-routing.module.ts
│   │   │   │   │       └── media.module.ts
│   │   │   │   │
│   │   │   │   ├── layout/                     # Layout components
│   │   │   │   │   ├── main-layout/
│   │   │   │   │   │   ├── main-layout.component.ts
│   │   │   │   │   │   ├── main-layout.component.html
│   │   │   │   │   │   └── main-layout.component.scss
│   │   │   │   │   ├── auth-layout/
│   │   │   │   │   └── seller-layout/
│   │   │   │   │
│   │   │   │   ├── app-routing.module.ts
│   │   │   │   ├── app.component.ts
│   │   │   │   ├── app.component.html
│   │   │   │   ├── app.component.scss
│   │   │   │   └── app.module.ts
│   │   │   │
│   │   │   ├── assets/
│   │   │   │   ├── images/
│   │   │   │   │   ├── logo.png
│   │   │   │   │   └── placeholder.png
│   │   │   │   ├── icons/
│   │   │   │   └── fonts/
│   │   │   │
│   │   │   ├── environments/
│   │   │   │   ├── environment.ts
│   │   │   │   ├── environment.development.ts
│   │   │   │   ├── environment.docker.ts
│   │   │   │   └── environment.prod.ts
│   │   │   │
│   │   │   ├── styles/
│   │   │   │   ├── styles.scss                 # Global styles
│   │   │   │   ├── _variables.scss             # Theme variables
│   │   │   │   ├── _mixins.scss
│   │   │   │   └── _theme.scss
│   │   │   │
│   │   │   ├── index.html
│   │   │   └── main.ts
│   │   │
│   │   ├── angular.json
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   ├── tsconfig.app.json
│   │   ├── tsconfig.spec.json
│   │   ├── karma.conf.js
│   │   ├── .eslintrc.json
│   │   ├── Dockerfile
│   │   ├── .dockerignore
│   │   ├── nginx.conf                          # For production build
│   │   └── README.md
│   │
│   └── e2e/                                    # End-to-end tests (Cypress/Playwright)
│       ├── cypress/
│       │   ├── e2e/
│       │   │   ├── auth/
│       │   │   │   ├── login.cy.ts
│       │   │   │   └── register.cy.ts
│       │   │   ├── products/
│       │   │   │   ├── product-list.cy.ts
│       │   │   │   └── product-detail.cy.ts
│       │   │   └── seller/
│       │   │       ├── create-product.cy.ts
│       │   │       └── upload-media.cy.ts
│       │   ├── fixtures/
│       │   ├── support/
│       │   └── cypress.config.ts
│       └── README.md
│
├── .env.shared                                 # Shared environment variables (committed)
├── .env.secrets                                # Secret credentials (GITIGNORED)
├── .env.secrets.example                        # Template for secrets
├── .gitignore
├── .dockerignore
├── docker-compose.yml                          # Local development orchestration
├── docker-compose.prod.yml                     # Production configuration
├── Makefile                                    # Convenience commands
├── pom.xml                                     # Parent POM (if using Maven multi-module)
├── README.md                                   # Main documentation
└── LICENSE
```

## 📦 Key Dependencies Summary

### Backend Services (pom.xml)

```xml
<!-- Common dependencies across all services -->
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Cloud -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-sleuth-zipkin</artifactId>
    </dependency>

    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Resilience4j (Circuit Breaker) -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot2</artifactId>
    </dependency>

    <!-- Lombok (reduce boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- MapStruct (object mapping) -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- Swagger/OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-ui</artifactId>
        <version>1.7.0</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>de.flapdoodle.embed</groupId>
        <artifactId>de.flapdoodle.embed.mongo</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Media Service Additional Dependencies

```xml
<!-- AWS S3/MinIO -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.26</version>
</dependency>

<!-- Image processing -->
<dependency>
    <groupId>org.imgscalr</groupId>
    <artifactId>imgscalr-lib</artifactId>
    <version>4.2</version>
</dependency>
```

### Frontend (package.json)

```json
{
  "dependencies": {
    "@angular/animations": "^17.0.0",
    "@angular/common": "^17.0.0",
    "@angular/compiler": "^17.0.0",
    "@angular/core": "^17.0.0",
    "@angular/forms": "^17.0.0",
    "@angular/material": "^17.0.0",
    "@angular/platform-browser": "^17.0.0",
    "@angular/platform-browser-dynamic": "^17.0.0",
    "@angular/router": "^17.0.0",
    "rxjs": "^7.8.0",
    "tslib": "^2.3.0",
    "zone.js": "^0.14.0",
    
    "@ngrx/store": "^17.0.0",
    "@ngrx/effects": "^17.0.0",
    "@ngrx/store-devtools": "^17.0.0",
    
    "ngx-toastr": "^18.0.0",
    "ngx-spinner": "^16.0.0"
  },
  "devDependencies": {
    "@angular-devkit/build-angular": "^17.0.0",
    "@angular/cli": "^17.0.0",
    "@angular/compiler-cli": "^17.0.0",
    "@types/jasmine": "~5.1.0",
    "@types/node": "^20.0.0",
    "jasmine-core": "~5.1.0",
    "karma": "~6.4.0",
    "karma-chrome-launcher": "~3.2.0",
    "karma-coverage": "~2.2.0",
    "karma-jasmine": "~5.1.0",
    "karma-jasmine-html-reporter": "~2.1.0",
    "typescript": "~5.2.2",
    
    "cypress": "^13.0.0",
    "eslint": "^8.0.0",
    "prettier": "^3.0.0"
  }
}
```

## 🚀 Quick Start Commands

```bash
# Clone repository
git clone https://github.com/your-org/buy01-platform.git
cd buy01-platform

# Setup environment
cp .env.secrets.example .env.secrets
# Edit .env.secrets with real values

# Start all services
chmod +x scripts/start-dev.sh
./scripts/start-dev.sh

# Or use Docker Compose directly
docker-compose --env-file .env.shared --env-file .env.secrets up --build

# Access services
# Eureka Dashboard: http://localhost:8761
# API Gateway: http://localhost:8080
# Frontend: http://localhost:4200
# MinIO Console: http://localhost:9001
```

## 📝 Notes

1. **Hexagonal Architecture** is applied consistently across User, Product, and Media services
2. **Domain-Driven Design** principles guide service boundaries
3. **Event-Driven Architecture** with Kafka enables loose coupling
4. **Angular Material** provides responsive UI components
5. **Docker Compose** orchestrates the entire stack locally
6. **Observability** built-in with actuator, Zipkin, and Prometheus ready
7. **Security** enforced at multiple layers (Gateway + Services)
8. **Testing** strategy includes unit, integration, and E2E tests

This structure supports your goal of becoming a **System Architect with DevOps mindset** by demonstrating:
- ✅ Proper service decomposition
- ✅ Clean architecture patterns
- ✅ Infrastructure as Code
- ✅ CI/CD readiness
- ✅ Observability practices
- ✅ Security best practices
