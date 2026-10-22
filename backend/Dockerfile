# ──────────────────────────────────────────────────────
# Stage 1: Build  (Maven + JDK 21)
# ──────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies before copying source
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B \
    && mv target/*.jar app.jar

# ──────────────────────────────────────────────────────
# Stage 2: Runtime  (JRE 21 Alpine – ~200 MB image)
# ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Install curl for Docker healthcheck
RUN apk add --no-cache curl

# Non-root user for security
RUN addgroup -S cms && adduser -S cms -G cms

WORKDIR /app

COPY --from=builder /build/app.jar app.jar

# Ensure non-root ownership
RUN chown -R cms:cms /app
USER cms

EXPOSE 8080

# Actuator health endpoint check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

# JVM production-tuned flags
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
