# Multi-stage build for Payment Gateway Mock
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent pom and all modules for Maven validation
COPY pom.xml ./
COPY shared-models/ ./shared-models/
COPY policy-service/ ./policy-service/
COPY billing-service/ ./billing-service/
COPY payment-service/ ./payment-service/
COPY notification-service/ ./notification-service/
COPY payment-gateway-mock/ ./payment-gateway-mock/

# Build the specific application
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