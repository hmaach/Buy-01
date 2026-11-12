# syntax=docker/dockerfile:1
# Build context: backend/ (the gateway needs the parent POM and shared modules).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

RUN apt-get update && apt-get install -y --no-install-recommends openssl && rm -rf /var/lib/apt/lists/*

COPY . .

ARG SSL_PASSWORD
RUN test -n "${SSL_PASSWORD}" && \
    RES=api-gateway/src/main/resources && \
    openssl req -x509 -newkey rsa:4096 \
    -keyout "$RES/key.pem" -out "$RES/cert.pem" \
    -sha256 -days 365 -nodes \
    -subj "/C=MA/ST=Oujda/L=Oujda/O=Zone01/OU=IT/CN=localhost" && \
    openssl pkcs12 -export \
    -in "$RES/cert.pem" -inkey "$RES/key.pem" \
    -out "$RES/edge-service.p12" \
    -name springgateway \
    -passout pass:${SSL_PASSWORD} && \
    rm "$RES/key.pem" "$RES/cert.pem"

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl api-gateway -am package -DskipTests && \
    cp api-gateway/target/api-gateway-*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre
# Run as an unprivileged user: a compromised app cannot act as root in the container.
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
