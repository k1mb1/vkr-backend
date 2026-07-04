# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

# Resolve dependencies first so this layer is cached across source-only changes.
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties lombok.config ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew --no-daemon -x test clean bootJar

FROM eclipse-temurin:25-jre
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

WORKDIR /app
EXPOSE 8080

# The `prod` profile is the application's default (spring.profiles.default),
# so no SPRING_PROFILES_ACTIVE is needed here.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
