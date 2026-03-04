# Buy-01

Repository: [https://github.com/hmaach/buy-01](https://github.com/hmaach/buy-01)

---

## Clone the Repository

```bash
git clone https://github.com/hmaach/buy-01.git
cd buy-01
```

Ensure you have the following installed:

- Git
- Docker & Docker Compose
- Java v21+ (compatible with your Maven configuration)
- Maven (optional, since `mvnw` is included)

---

## Usage

All commands must be executed from the project root directory.

### Build & Clean

```bash
make install      # clean install all services
make clean        # clean all services
make rebuild      # full rebuild (clean + install)
```

### Docker Management

```bash
make docker-build # build Docker images
make docker-up    # start the stack in detached mode
make docker-down  # stop the stack
```

### Recommended Workflow

```bash
make rebuild
make docker-build
make docker-up
```

---

## Actuator Health Endpoints

The following endpoints expose health information for each service.

### API Gateway

```
http://localhost:8080/actuator/health
```

### User Service

```
http://localhost:8081/actuator/health
```

### Product Service

```
http://localhost:8082/actuator/health
```

### Media Service

```
http://localhost:8083/actuator/health
```

Each endpoint returns the service health status. These endpoints can be used for:

- Monitoring
- Readiness and liveness checks
- Integration with service discovery systems
- Observability tools (e.g., Prometheus)
- Load balancer health checks

---
