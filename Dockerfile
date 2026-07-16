# syntax=docker/dockerfile:1

# ---- Build stage -------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

# Copy only the build definition first so the (slow) dependency-resolution layer
# is reused whenever just the sources change. gradle.lockfile is required: the
# build locks every configuration, so it must reach the build context to stay
# reproducible.
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties gradle.lockfile lombok.config ./
RUN chmod +x gradlew \
    && ./gradlew --no-daemon dependencies || true

# Build the Spring Boot jar. Tests are the CI job's responsibility, not the image.
COPY src ./src
RUN ./gradlew --no-daemon -x test bootJar

# Explode the fat jar into Spring Boot layers so the runtime image keeps
# third-party dependencies in their own cached layers, separate from the
# frequently-changing application code. The jar is renamed to a stable name so
# the runtime ENTRYPOINT does not depend on the project version.
RUN JAR="$(find build/libs -maxdepth 1 -name '*.jar' -not -name '*-plain.jar')" \
    && cp "$JAR" app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# ---- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Run as an unprivileged user.
RUN groupadd --system spring && useradd --system --gid spring spring

# Copy the layers most-stable first, so a code change only invalidates the last.
COPY --from=builder --chown=spring:spring /workspace/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /workspace/extracted/application/ ./

USER spring:spring
EXPOSE 8080

# The `prod` profile is the application default (spring.profiles.default=prod),
# so no SPRING_PROFILES_ACTIVE is needed. MaxRAMPercentage lets the JVM size its
# heap from the container's memory limit; override JAVA_OPTS to tune at runtime.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
