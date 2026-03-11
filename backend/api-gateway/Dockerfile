# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Install openssl
RUN apk add --no-cache openssl

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Generate SSL certificate into resources so it gets bundled in the jar
ARG SSL_PASSWORD=123456
RUN mkdir -p src/main/resources && \
    openssl req -x509 -newkey rsa:4096 \
        -keyout src/main/resources/key.pem \
        -out src/main/resources/cert.pem \
        -sha256 -days 365 -nodes \
        -subj "/C=MA/ST=Oujda/L=Oujda/O=Zone01/OU=IT/CN=localhost" && \
    openssl pkcs12 -export \
        -in src/main/resources/cert.pem \
        -inkey src/main/resources/key.pem \
        -out src/main/resources/edge-service.p12 \
        -name springgateway \
        -passout pass:${SSL_PASSWORD} && \
    rm src/main/resources/key.pem src/main/resources/cert.pem

# Build the application (skipping tests for speed)
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]