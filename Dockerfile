FROM maven:3-eclipse-temurin-25 AS builder
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
COPY --from=builder /workspace/target/*.jar /app/app.jar

WORKDIR /app
EXPOSE 8080

# The `prod` profile is the application's default (spring.profiles.default),
# so no SPRING_PROFILES_ACTIVE is needed here.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
