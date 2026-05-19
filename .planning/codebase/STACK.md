# Technology Stack

**Analysis Date:** 2026-05-19

## Languages

**Primary:**
- Java 21 (target compilation level, declared in `pom.xml` `<java.version>21</java.version>`) - all application source

**Runtime JVM (local):**
- Oracle GraalVM 25.0.2 (environment JVM; container target is Java 21)

## Runtime

**Environment:**
- JVM — Spring Boot embedded Tomcat (via `spring-boot-starter-webmvc`)
- Default port: `8081` (configurable via `PORT` env var); Docker image exposes `8080`

**Package Manager:**
- Apache Maven 3.9.12
- Wrapper: `mvnw` / `mvnw.cmd` (`.mvn/wrapper/maven-wrapper.properties`)
- Lockfile: No Maven lockfile (dependency versions resolved via BOM from Spring Boot parent POM)

## Frameworks

**Core:**
- Spring Boot 4.0.2 (parent POM) — application framework
- Spring Web MVC (`spring-boot-starter-webmvc`) — REST API layer
- Spring Data JPA (`spring-boot-starter-data-jpa`) — ORM/repository layer
- Spring Security (`spring-boot-starter-security-oauth2-resource-server`) — authentication and method-level authorization

**Database Migrations:**
- Liquibase (`spring-boot-starter-liquibase`) — schema versioning, changesets in `src/main/resources/db/changelog/`

**API Documentation:**
- SpringDoc OpenAPI 3.0.2 (`springdoc-openapi-starter-webmvc-ui`) — auto-generated Swagger UI at `/swagger-ui.html`, API docs at `/v3/api-docs`

**Testing:**
- JUnit 5 (Jupiter) — via `spring-boot-starter-*-test` BOM dependencies
- Spring Boot Test — context loading and integration tests
- Test starters: `spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-security-oauth2-resource-server-test`, `spring-boot-starter-liquibase-test`, `spring-boot-starter-validation-test`

**Build/Dev:**
- Spring Boot Maven Plugin — packaging, AOT processing (`process-aot` execution)
- Docker + Dockerfile (multi-stage build) — containerization

## Key Dependencies

**Critical:**
- `org.postgresql:postgresql` (runtime scope) — PostgreSQL 17 JDBC driver; version managed by Spring Boot BOM
- `org.springframework.boot:spring-boot-starter-security-oauth2-resource-server` — JWT-based OAuth2 resource server; validates tokens via JWKS from issuer
- `org.springframework.boot:spring-boot-starter-data-jpa` — Hibernate ORM; `ddl-auto: validate` (schema must match, Liquibase owns DDL)

**Code Generation:**
- `org.projectlombok:lombok` — boilerplate reduction (`@Slf4j`, `@Value`, field defaults, chain accessors); configured in `lombok.config`
- `org.mapstruct:mapstruct` 1.6.3 — compile-time DTO/entity mapping; annotation processor wired with `lombok-mapstruct-binding:0.2.0`

**Validation:**
- `spring-boot-starter-validation` — Bean Validation (Jakarta Validation API) for request DTOs

## Configuration

**Environment (required env vars):**
- `SPRING_DATASOURCE_URL` — JDBC URL for PostgreSQL
- `SPRING_DATASOURCE_USERNAME` — database username
- `SPRING_DATASOURCE_PASSWORD` — database password
- `OAUTH2_RESOURSESERVER_ISSUER_URI` — OAuth2 JWT issuer URI (note: typo "RESOURSESERVER" in var name)
- `CORS_ALLOWED_ORIGINS` — comma-separated list of allowed origins
- `PORT` (optional) — HTTP port override (default `8081`)

**Profiles:**
- `dev` (`application-dev.yaml`) — SQL debug logging, Hibernate format SQL, no second-level cache
- `prod` (`application-prod.yaml`) — SQL logging off, response compression on, Swagger UI disabled, error details suppressed
- `test` (`application-test.yaml`) — JWT issuer points to `http://localhost:8080`, CORS to `http://localhost:3000`

**Build:**
- `pom.xml` — single-module Maven project
- `Dockerfile` — multi-stage; builder image `maven:3.9.12-eclipse-temurin-21-alpine`, runtime image `gcr.io/distroless/java21-debian12`; `SPRING_PROFILES_ACTIVE=prod` baked in

## JPA / Hibernate Configuration

- `open-in-view: false` — no open session in view anti-pattern
- `ddl-auto: validate` — Hibernate validates schema against Liquibase-managed DDL; never auto-creates tables
- Batch inserts/updates enabled: `batch_size: 50`, `order_inserts: true`, `order_updates: true`
- Default batch fetch size: 30

## Platform Requirements

**Development:**
- Java 21+
- Maven 3.9.12 (or use `./mvnw`)
- PostgreSQL 17 (available via `docker-compose.yaml`)
- An OAuth2/OIDC provider reachable at the issuer URI

**Production:**
- Docker (distroless Java 21 image)
- External PostgreSQL 17
- External OAuth2/OIDC provider
- Deployment target: any container runtime (no cloud-specific SDK detected)

---

*Stack analysis: 2026-05-19*
