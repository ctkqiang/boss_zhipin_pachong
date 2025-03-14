FROM maven:3.8-openjdk-11-slim AS builder

FROM openjdk:25-jdk-slim

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["mvn", "spring-boot:run"]

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/ || exit 1
