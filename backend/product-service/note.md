```
product-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/productservice/
│   │   │   ├── application/              # 🚗 DRIVING ADAPTERS (Inbound)
│   │   │   │   └── web/
│   │   │   │       ├── controller/       # ProductController.java
│   │   │   │       ├── dto/              # Request/Response objects
│   │   │   │       └── mapper/           # Web mappers (DTO <-> Domain)
│   │   │   ├── domain/                   # 🧠 THE CORE (Pure Java logic)
│   │   │   │   ├── model/                # Product.java, Price.java
│   │   │   │   ├── ports/
│   │   │   │   │   ├── in/               # ProductUseCase.java (Interfaces)
│   │   │   │   │   └── out/              # ProductRepositoryPort.java
│   │   │   │   └── service/              # ProductServiceImpl.java
│   │   │   ├── infrastructure/           # ⚙️ DRIVEN ADAPTERS (Outbound)
│   │   │   │   ├── persistence/
│   │   │   │   │   └── mongo/            # MongoRepository & EntityMappers
│   │   │   │   └── messaging/            # KafkaProducerAdapter.java
│   │   │   └── config/                   # 🔌 THE GLUE (Spring Beans)
│   │   │       └── BeanConfiguration.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/                             # 🧪 Logic & Integration tests
└── pom.xml
```
```curl -H "Authorization: Bearer <your-jwt>" http://localhost:8080/products -X POST -d '{"name":"test"}'```