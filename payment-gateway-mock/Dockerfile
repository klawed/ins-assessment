# Multi-stage build for Payment Gateway Mock
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent pom first for better caching
COPY pom.xml ./

# Copy payment gateway mock
COPY payment-gateway-mock/pom.xml ./payment-gateway-mock/
COPY payment-gateway-mock/src ./payment-gateway-mock/src

# Build the application
RUN mvn clean package -DskipTests -pl payment-gateway-mock -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy the built jar
COPY --from=build /app/payment-gateway-mock/target/*.jar app.jar

# Change ownership to appuser
RUN chown appuser:appgroup app.jar

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]