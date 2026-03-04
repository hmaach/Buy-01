# Buy-01


## Actuator Health Endpoints

The following endpoints are available to monitor the health of each service in the system:

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

Each endpoint returns the health status of its corresponding service and can be used for monitoring, readiness checks, and integration with tools like Eureka, Prometheus, or load balancers.
