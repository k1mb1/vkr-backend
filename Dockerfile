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

# Оптимизации памяти для контейнера
# -Xmx256m / -Xms128m          — жесткий лимит кучи
# -XX:MaxMetaspaceSize=128m    — лимит классов/метаданных
# -XX:+UseSerialGC             — легковесный GC, меньше RAM-оверхеда
# -Dspring.jmx.enabled=false   — отключаем JMX (не нужен в контейнере)
# -Xss256k                     — уменьшаем стек потока (дефолт 1 МБ)
# -XX:MaxDirectMemorySize=64m  — лимит off-heap/direct buffers
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Dspring.jmx.enabled=false -Xss256k -XX:MaxDirectMemorySize=64m"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
