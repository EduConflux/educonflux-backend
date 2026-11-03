# syntax=docker/dockerfile:1

# ========================================================
# Stage 1: Build the Application with Maven & Temurin JDK 21
# ========================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project source code
COPY src ./src

# Build production JAR (skipping unit tests since tests are verified in CI)
RUN mvn clean package -DskipTests -B

# ========================================================
# Stage 2: Minimal Production Runtime with Temurin JRE 21
# ========================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add curl for healthchecks and debugging
RUN apk add --no-cache curl

# Security: Run as a non-privileged system user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Render assigns a dynamic port via the $PORT environment variable
EXPOSE 8080

# Production JVM flags optimized for container environments (e.g., Render 512MB RAM tier)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=40.0 -XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE=prod

# Launch Spring Boot application binding to Render's dynamic $PORT and active profile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

