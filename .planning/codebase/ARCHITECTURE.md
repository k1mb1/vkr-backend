<!-- refreshed: 2026-05-19 -->
# Architecture

**Analysis Date:** 2026-05-19

## System Overview

```text
┌──────────────────────────────────────────────────────────────────────────┐
│                           HTTP Clients / QR Scanner                      │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │
                                 ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                          Web Layer (Controllers)                          │
│  `attendance/web/`  `lesson/web/`  `group/web/`  `subject/web/`          │
│  `teacher/web/`     `student/internal/web/`       `checkin/web/`         │
└──────┬───────────────────────────────────────────────────────────────────┘
       │  delegates via public API interface (e.g. AttendanceApi, LessonApi)
       ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                        Service Layer (internal/)                          │
│  `attendance/internal/AttendanceService`                                  │
│  `attendance/checkin/internal/CheckInSessionService`                      │
│  `lesson/internal/LessonService`  `lesson/internal/LessonStudentsService` │
│  `group/internal/GroupService`    `subject/internal/SubjectService`       │
│  `teacher/internal/TeacherService` `student/internal/StudentService`      │
└──────┬───────────────────────────────────────────────────────────────────┘
       │  queries via Spring Data JPA repositories
       ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                      Repository Layer (internal/)                         │
│  `*Repository.java` — Spring Data JPA, custom JPQL/Specification queries  │
└──────┬───────────────────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────────────────┐
│               PostgreSQL — managed by Liquibase migrations                │
│               `src/main/resources/db/changelog/v0/`                      │
└──────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| `VkrBackendApplication` | Spring Boot entry point | `src/main/java/com/github/k1mb1/vkr_backend/VkrBackendApplication.java` |
| `*Controller` | HTTP binding, request/response mapping, delegates to `*Api` | `*/web/*Controller.java` |
| `*Api` interface | Public contract per domain module, decouples controller from service | `*/{ModuleName}Api.java` |
| `*Service` (internal) | Business logic, implements `*Api` | `*/internal/*Service.java` |
| `*Repository` | Data access via Spring Data JPA | `*/internal/*Repository.java` |
| `*Mapper` (MapStruct) | Entity ↔ DTO conversion | `*/internal/*Mapper.java` |
| `GlobalExceptionHandler` | Centralised HTTP error translation | `src/main/java/com/github/k1mb1/vkr_backend/common/error/GlobalExceptionHandler.java` |
| `SecurityConfig` | JWT OAuth2 resource server, CORS, stateless sessions | `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java` |
| `JwtAuthConverter` | Extracts authorities from JWT claims | `src/main/java/com/github/k1mb1/vkr_backend/configs/JwtAuthConverter.java` |
| `SecurityService` | Per-request JWT subject access (`@securityService.isSameUser(...)`) | `src/main/java/com/github/k1mb1/vkr_backend/common/security/SecurityService.java` |
| `*ReferenceService` interface | Cross-module entity reference access without exposing repositories | `group/GroupReferenceService.java`, `teacher/TeacherReferenceService.java`, `subject/SubjectReferenceService.java` |
| `LessonStudentsApi` | Derives lesson audience from scopes or subject groups | `src/main/java/com/github/k1mb1/vkr_backend/lesson/LessonStudentsApi.java` |

## Pattern Overview

**Overall:** Modular Layered Architecture with Interface-Based Cross-Module Decoupling

**Key Characteristics:**
- Each domain (attendance, checkin, group, lesson, student, subject, teacher) is a self-contained package
- Services live in `internal/` and are package-private by convention; the public `*Api` interface is the only cross-module contract
- `*ReferenceService` interfaces allow one domain to access another domain's entities without importing internal repositories
- MapStruct mappers perform all entity-to-DTO conversion; controllers never touch entities directly
- JPA entities use a three-level hierarchy: `Auditable` → `BaseEntity` → `ArchivableEntity` (soft-delete via `@SQLRestriction`)

## Layers

**Web Layer:**
- Purpose: HTTP endpoint definitions, validation, OpenAPI annotations
- Location: `*/web/`
- Contains: `@RestController` classes, request POJOs (records), response POJOs (records), filter objects for query params
- Depends on: public `*Api` interface of the same module
- Used by: HTTP clients

**Service Layer:**
- Purpose: Business logic implementation
- Location: `*/internal/`
- Contains: `@Service` classes implementing `*Api`, business rules, orchestration across repositories and other module APIs
- Depends on: own `*Repository`, own `*Mapper`, other modules' `*Api` or `*ReferenceService` interfaces
- Used by: controllers (via the `*Api` interface)

**Domain Layer:**
- Purpose: JPA entity definitions
- Location: `*/domain/`
- Contains: `@Entity` classes, enums, `@MappedSuperclass` base classes
- Depends on: nothing above domain layer
- Used by: repositories, mappers, services

**Repository Layer:**
- Purpose: Database access
- Location: `*/internal/`
- Contains: Spring Data JPA `*Repository` interfaces with custom queries, JPA `Specification` classes
- Depends on: domain entities
- Used by: services within the same module only

**Common / Cross-cutting:**
- Purpose: Shared base types, error handling, security utilities, Spring configs
- Location: `common/domain/`, `common/error/`, `common/security/`, `configs/`
- Contains: `BaseEntity`, `Auditable`, `ArchivableEntity`, `GlobalExceptionHandler`, `SecurityService`, `SecurityConfig`, `JwtAuthConverter`, `JpaAuditingConfig`, `OpenApiConfig`

## Data Flow

### Primary REST Request Path

1. HTTP request arrives → Spring Security validates JWT (`JwtAuthConverter`) (`configs/JwtAuthConverter.java`)
2. Request routes to `@RestController` — e.g., `AttendanceController` (`attendance/web/AttendanceController.java`)
3. Controller validates request body/params via Bean Validation (`@Valid`) and delegates to `AttendanceApi`
4. `AttendanceService` (implements `AttendanceApi`) performs business logic, calls repositories (`attendance/internal/AttendanceService.java`)
5. MapStruct mapper converts entity → response DTO (`attendance/internal/AttendanceMapper.java`)
6. Controller wraps result in `ResponseEntity` and returns JSON

### Check-In Session Flow (student self-check-in via QR)

1. Teacher creates a `CheckInSession` via `POST /api/check-in-sessions` → `CheckInSessionController.start()`
2. QR code URL encodes session UUID → student opens `GET /api/check-in-sessions/public/{id}` (unauthenticated)
3. Student POSTs `POST /api/check-in-sessions/public/{id}/check-in` — `CheckInSessionService.checkIn()` validates time window, records `CheckInRecord`
4. Teacher previews via `GET /api/check-in-sessions/{id}/preview` (proposed statuses)
5. Teacher confirms via `POST /api/check-in-sessions/{id}/confirm` → `CheckInSessionService.confirm()` calls `AttendanceApi.upsert()` for every student in lesson audience
6. Attendance records are persisted in `attendances` table

### Cross-Module Service Dependency Flow

```
GroupService (internal)
  └─ calls StudentApi.createStudent / archiveStudent / deleteStudentsByGroup

LessonService (internal)
  └─ calls GroupReferenceService.getGroupReferenceById / getSubgroupReferenceById

AttendanceService (internal)
  └─ calls LessonStudentsApi.studentsOf(lesson)
  └─ reads TeacherSubjectPermissionRepository (subject module)

CheckInSessionService (internal)
  └─ calls LessonStudentsApi.studentsOf(lesson)
  └─ calls AttendanceApi.upsert(...)
```

**State Management:**
- Fully stateless HTTP (JWT, `SessionCreationPolicy.STATELESS`)
- No in-memory cache; all state in PostgreSQL
- Soft-delete: `ArchivableEntity.archive()` sets `archived_at`; `@SQLRestriction("archived_at IS NULL")` filters rows transparently on all JPA queries

## Key Abstractions

**`BaseEntity`:**
- Purpose: UUID primary key + auditing timestamps for all JPA entities
- Examples: extended by all domain entities (`Student`, `Group`, `Subject`, `Lesson`, etc.)
- Pattern: `@MappedSuperclass`, `@UuidGenerator`, extends `Auditable`
- File: `src/main/java/com/github/k1mb1/vkr_backend/common/domain/BaseEntity.java`

**`ArchivableEntity`:**
- Purpose: Soft-delete support — entities with `archived_at` column
- Examples: `Student`, `Subject`, `Lesson`, `CheckInSession`
- Pattern: extends `BaseEntity`; `@SQLRestriction("archived_at IS NULL")` on entity suppresses archived rows from all JPQL/Criteria queries
- File: `src/main/java/com/github/k1mb1/vkr_backend/common/domain/ArchivableEntity.java`

**Public `*Api` Interface:**
- Purpose: Each domain module exposes exactly one (or a few) interfaces at the module root; services implement them; controllers and other modules depend on the interface only
- Examples: `AttendanceApi`, `LessonApi`, `LessonStudentsApi`, `GroupsApi`, `SubjectsApi`, `TeachersApi`, `StudentApi`, `CheckInSessionApi`
- Pattern: interface in module root package; implementing `@Service` in `internal/` subpackage

**`*ReferenceService` Interface:**
- Purpose: Allows cross-module entity reference access (JPA proxy, no full load) without exposing internal repositories
- Examples: `GroupReferenceService`, `TeacherReferenceService`, `SubjectReferenceService`
- Pattern: interface in module root; `*ReferenceServiceImpl` in `internal/` calls `repository.getReferenceById()`

**JPA Specification (`*Specifications`):**
- Purpose: Composable, type-safe filtering for list queries
- Examples: `LessonSpecifications`, `GroupSpecifications`, `SubjectSpecifications`, `TeacherSpecification`
- Pattern: static factory methods returning `Specification<Entity>`; used in `findAll(spec, sort)` calls

**Filter Objects:**
- Purpose: Query parameter binding for filtered list endpoints
- Examples: `AttendanceFilter`, `LessonFilter`, `GroupFilter`, `SubjectFilter`, `TeacherFilter`
- Pattern: Java record annotated with `@ModelAttribute`; used with `@ParameterObject` for OpenAPI

## Entry Points

**Application Entry Point:**
- Location: `src/main/java/com/github/k1mb1/vkr_backend/VkrBackendApplication.java`
- Triggers: JVM startup
- Responsibilities: Bootstrap Spring context via `@SpringBootApplication`

**REST API Endpoints:**
- `GET/PUT /api/attendances` — `attendance/web/AttendanceController.java`
- `GET/POST /api/check-in-sessions`, `/api/check-in-sessions/{id}`, `/{id}/confirm`, `/{id}/cancel`, `/{id}/preview` — `attendance/checkin/web/CheckInSessionController.java`
- `GET/POST /api/check-in-sessions/public/{id}`, `/public/{id}/check-in` — `attendance/checkin/web/PublicCheckInController.java` (unauthenticated)
- `GET/POST/PATCH/DELETE /api/groups` — `group/web/GroupsController.java`
- `GET/POST/DELETE /api/subgroups` — `group/web/SubgroupsController.java`
- `GET/POST/PATCH/DELETE /api/lessons` — `lesson/web/LessonController.java`
- `GET/POST/DELETE /api/subjects` — `subject/web/SubjectsController.java`
- `GET/POST/PATCH/DELETE /api/teachers` — `teacher/web/TeachersController.java`
- `GET/POST/PATCH/DELETE /api/teacher-subject-permissions` — `subject/web/TeacherSubjectPermissionsController.java`

## Architectural Constraints

- **Threading:** Spring MVC — servlet-per-request thread pool (no reactive/WebFlux). JDBC batch inserts are enabled (`batch_size=50`).
- **Global state:** None beyond Spring singleton beans. `SecurityService` reads `SecurityContextHolder` (thread-local).
- **Circular imports:** No detected circular module dependencies. Cross-module calls flow only through `*Api` and `*ReferenceService` interfaces.
- **Hibernate lazy loading:** `open-in-view: false` — all associations must be fetched within transaction bounds in the service layer.
- **Schema management:** Liquibase owns DDL; `hibernate.ddl-auto: validate` ensures schema matches entities at startup.

## Anti-Patterns

### Accessing Internal Repository Across Modules

**What happens:** `AttendanceService` directly imports `TeacherSubjectPermissionRepository` from the `subject` module and `LessonRepository` from the `lesson` module.
**Why it's wrong:** It bypasses the module boundary, tightly coupling `attendance` internals to `subject` and `lesson` internals. Changes to those repositories can silently break `AttendanceService`.
**Do this instead:** Expose a dedicated `*Api` method on `SubjectApi` or `LessonApi` for the data access needed. See how `LessonStudentsApi` correctly encapsulates student-of-lesson resolution (`lesson/LessonStudentsApi.java`).

### Inconsistent web sub-package layout for `student`

**What happens:** The `student` module places its web DTOs in `student/internal/web/` rather than `student/web/`.
**Why it's wrong:** Breaks the consistent module layout every other domain follows (`*/web/requests/`, `*/web/responses/`), making code navigation unintuitive.
**Do this instead:** Move `student/internal/web/` contents to `student/web/requests/` and `student/web/responses/`.

## Error Handling

**Strategy:** Centralised `@RestControllerAdvice` — `GlobalExceptionHandler` catches all exceptions and maps them to a structured `ErrorDto`.

**Patterns:**
- `EntityNotFoundException` → 404 with `ErrorCode.NOT_FOUND`
- `MethodArgumentNotValidException` / `ConstraintViolationException` → 400 with field-level `FieldError` list
- `IllegalArgumentException` → 400 with `ErrorCode.ILLEGAL_ARGUMENT`
- `IllegalStateException` → 400 with `ErrorCode.ILLEGAL_STATE`
- `DataIntegrityViolationException` → 409 with `ErrorCode.CONFLICT`
- `AccessDeniedException` → 403 with `ErrorCode.ACCESS_DENIED`
- `Exception` (fallback) → 500 with `ErrorCode.INTERNAL_ERROR` (stack trace logged at ERROR level)
- Services throw standard JDK/JPA exceptions; they do not define custom exception types

## Cross-Cutting Concerns

**Logging:** SLF4J with Lombok `@Slf4j`. Used in `GlobalExceptionHandler` for WARN/ERROR on exceptions. Services do not add logging by default.

**Validation:** Jakarta Bean Validation on all request records. Controller methods are annotated `@Valid`. URL path parameters validated via `@Validated` on controller class where needed.

**Authentication:** OAuth2 JWT resource server. All endpoints require a valid JWT except `PUBLIC_ENDPOINTS` (`/swagger-ui/**`, `/v3/api-docs/**`) and `/api/check-in-sessions/public/**` (effectively open — `anyRequest().permitAll()` is current config, method security is available via `@EnableMethodSecurity`).

---

*Architecture analysis: 2026-05-19*
