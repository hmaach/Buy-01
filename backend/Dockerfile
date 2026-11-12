# syntax=docker/dockerfile:1
# Shared image for discovery-server, user-service, product-service and media-service.
# Build context: backend/. Select the module with --build-arg SERVICE=<module>.
FROM maven:3.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    test -n "${SERVICE}" && \
    mvn -B -q -pl "${SERVICE}" -am package -DskipTests && \
    cp "${SERVICE}/target/${SERVICE}-"*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre
# Run as an unprivileged user: a compromised app cannot act as root in the container.
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/app.jar app.jar
USER app
ENTRYPOINT ["java", "-jar", "app.jar"]
