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
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
