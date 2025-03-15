# Use a stable Java version
FROM openjdk:11-jdk-slim

# Set working directory
WORKDIR /app

# Copy maven files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Make mvnw executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw package -DskipTests

# Expose port 8080
EXPOSE 8080

# Add environment variable for Spring
ENV SPRING_PROFILES_ACTIVE=prod

# Set the startup command
CMD ["java", "-jar", "target/boss_zhipin_pachong-0.0.1-SNAPSHOT.jar"]

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/ || exit 1
