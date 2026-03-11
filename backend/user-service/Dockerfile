# Stage 1: Build stage 
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy maven executable and pom
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage 
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Create the upload directory
RUN mkdir -p /app/uploads/avatars

EXPOSE 8081

# Since you have 8GB RAM, keeping these memory limits is a good idea
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]