# Use lightweight JDK
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the built Spring Boot JAR
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]