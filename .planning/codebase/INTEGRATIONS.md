# External Integrations

**Analysis Date:** 2026-05-19

## APIs & External Services

**None detected beyond infrastructure dependencies below.** The application does not call external third-party REST APIs. All domain logic is self-contained.

## Data Storage

**Databases:**
- PostgreSQL 17
  - Connection: `${SPRING_DATASOURCE_URL}`, `${SPRING_DATASOURCE_USERNAME}`, `${SPRING_DATASOURCE_PASSWORD}`
  - Client: Spring Data JPA (Hibernate 6 ORM)
  - Driver: `org.postgresql:postgresql` (runtime dependency)
  - Local dev: `docker-compose.yaml` spins up `postgres:17-alpine` on port `5432`, DB name `postgres`, user `user`
  - Schema management: Liquibase, changesets under `src/main/resources/db/changelog/v0/`

**File Storage:**
- Not used — no S3, GCS, or local file storage integration detected.

**Caching:**
- No caching layer detected. Hibernate second-level cache is explicitly disabled in both dev and prod profiles (`use_second_level_cache: false`). No Redis, Memcached, or Spring Cache integration.

## Authentication & Identity

**Auth Provider:**
- External OAuth2/OIDC provider (provider-agnostic; compatible with Keycloak, Auth0, etc.)
  - Role: OAuth2 Resource Server — the application validates JWTs issued by a third-party provider; it does NOT issue tokens itself.
  - Implementation: `spring-boot-starter-security-oauth2-resource-server`; JWKS endpoint discovered automatically via `issuer-uri`
  - Issuer URI config: `${OAUTH2_RESOURSESERVER_ISSUER_URI}` (env var, note typo "RESOURSESERVER")
  - JWT conversion: `src/main/java/com/github/k1mb1/vkr_backend/configs/JwtAuthConverter.java` — extracts granted authorities from JWT claims via `JwtGrantedAuthoritiesConverter`; resource roles extraction is currently a stub returning empty set.
  - User identity: JWT `sub` claim used as user UUID in `src/main/java/com/github/k1mb1/vkr_backend/common/security/SecurityService.java`
  - Session policy: STATELESS (no server-side session)
  - Method security: `@EnableMethodSecurity` active; `@PreAuthorize` annotations use `@securityService` bean

**API security scheme (OpenAPI):** Bearer JWT in `Authorization` header, documented in `src/main/java/com/github/kkimb1/vkr_backend/configs/OpenApiConfig.java`

## Monitoring & Observability

**Error Tracking:**
- Not detected. No Sentry, Datadog, or similar integration.

**Logs:**
- SLF4J + Logback (bundled with Spring Boot)
  - Dev profile: `org.hibernate.SQL` at DEBUG, `org.hibernate.orm.jdbc.bind` at TRACE, `org.springframework.web` at DEBUG, application package at DEBUG
  - Prod profile: root at WARN, application package at INFO, SQL logging suppressed

**Metrics / Tracing:**
- Not detected. No Micrometer, Spring Actuator, OpenTelemetry, or APM agent configured.

**PostgreSQL stats extension:** `pg_stat_statements` loaded in dev Docker Compose (`shared_preload_libraries=pg_stat_statements`) for query performance monitoring during development.

## CI/CD & Deployment

**Hosting:**
- Docker container (distroless Java 21 runtime image `gcr.io/distroless/java21-debian12`)
- No cloud-provider-specific SDK or deployment manifest detected (no Kubernetes YAML, no Helm chart, no cloud CLI config)

**CI Pipeline:**
- Not detected. No `.github/workflows/`, `.gitlab-ci.yml`, or similar CI configuration files found.

## Webhooks & Callbacks

**Incoming:**
- Check-in sessions expose a public endpoint (no auth required) at `src/main/java/com/github/k1mb1/vkr_backend/attendance/checkin/web/PublicCheckInController.java` — used for student self-check-in flows, not a webhook.

**Outgoing:**
- Not detected. No HTTP client calls to external webhook endpoints.

## API Documentation

**Swagger UI:**
- Available in non-prod profiles at `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Disabled in prod profile (`springdoc.api-docs.enabled: false`, `swagger-ui.enabled: false`)
- Configured in `src/main/java/com/github/k1mb1/vkr_backend/configs/OpenApiConfig.java`

## CORS

- Allowed origins: `${CORS_ALLOWED_ORIGINS}` env var (comma-separated)
- Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Credentials allowed: true
- Preflight cache: 3600 seconds
- Configured in `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java`

## Environment Configuration Summary

**Required env vars:**
- `SPRING_DATASOURCE_URL` — PostgreSQL JDBC URL
- `SPRING_DATASOURCE_USERNAME` — PostgreSQL username
- `SPRING_DATASOURCE_PASSWORD` — PostgreSQL password
- `OAUTH2_RESOURSESERVER_ISSUER_URI` — OAuth2/OIDC issuer URI (note: "RESOURSESERVER" typo in name)
- `CORS_ALLOWED_ORIGINS` — allowed CORS origins

**Optional env vars:**
- `PORT` — HTTP server port (default `8081`)

**Secrets location:**
- All secrets injected as environment variables at runtime; no secrets files in repo.

---

*Integration audit: 2026-05-19*
