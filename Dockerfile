FROM maven:3.9.12-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM gcr.io/distroless/java21-debian12
COPY --from=builder /workspace/target/*.jar /app/app.jar

WORKDIR /app
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
