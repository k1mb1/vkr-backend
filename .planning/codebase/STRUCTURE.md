# Codebase Structure

**Analysis Date:** 2026-05-19

## Directory Layout

```
vkr-backend/
├── src/
│   ├── main/
│   │   ├── java/com/github/k1mb1/vkr_backend/
│   │   │   ├── VkrBackendApplication.java       # Spring Boot entry point
│   │   │   ├── common/
│   │   │   │   ├── domain/                      # Shared base JPA entities
│   │   │   │   │   ├── Auditable.java           # createdAt / updatedAt
│   │   │   │   │   ├── BaseEntity.java          # UUID PK + equals/hashCode
│   │   │   │   │   └── ArchivableEntity.java    # Soft-delete (archivedAt)
│   │   │   │   ├── error/                       # Error types and global handler
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   ├── ErrorDto.java
│   │   │   │   │   ├── ErrorCode.java
│   │   │   │   │   ├── FieldError.java
│   │   │   │   │   └── ErrorMessages.java
│   │   │   │   └── security/
│   │   │   │       └── SecurityService.java     # JWT subject extraction helper
│   │   │   ├── configs/                         # Spring configuration beans
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthConverter.java
│   │   │   │   ├── JpaAuditingConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── attendance/                      # Attendance domain module
│   │   │   │   ├── AttendanceApi.java           # Public API interface
│   │   │   │   ├── domain/                      # Attendance, AttendanceStatus
│   │   │   │   ├── internal/                    # AttendanceService, AttendanceRepository, AttendanceMapper
│   │   │   │   ├── web/
│   │   │   │   │   ├── AttendanceController.java
│   │   │   │   │   ├── filters/                 # AttendanceFilter
│   │   │   │   │   ├── requests/                # UpsertAttendanceRequest
│   │   │   │   │   └── responses/               # AttendanceTableResponse, AttendanceCellResponse, ...
│   │   │   │   └── checkin/                     # Check-in sub-domain
│   │   │   │       ├── CheckInSessionApi.java   # Public API interface
│   │   │   │       ├── domain/                  # CheckInSession, CheckInRecord, enums
│   │   │   │       ├── internal/                # CheckInSessionService, repositories, mapper
│   │   │   │       └── web/
│   │   │   │           ├── CheckInSessionController.java  # Teacher-facing
│   │   │   │           ├── PublicCheckInController.java   # Student QR-code facing
│   │   │   │           ├── requests/
│   │   │   │           └── responses/
│   │   │   ├── group/                           # Group domain module
│   │   │   │   ├── GroupsApi.java
│   │   │   │   ├── SubgroupsApi.java
│   │   │   │   ├── GroupReferenceService.java   # Cross-module reference interface
│   │   │   │   ├── domain/                      # Group, Subgroup
│   │   │   │   ├── internal/                    # GroupService, SubgroupService, repositories, mappers, specs
│   │   │   │   └── web/
│   │   │   │       ├── GroupsController.java
│   │   │   │       ├── SubgroupsController.java
│   │   │   │       ├── filters/
│   │   │   │       ├── requests/
│   │   │   │       └── response/                # Note: inconsistent — "response" not "responses"
│   │   │   ├── lesson/                          # Lesson domain module
│   │   │   │   ├── LessonApi.java
│   │   │   │   ├── LessonStudentsApi.java       # Derived student audience per lesson
│   │   │   │   ├── domain/                      # Lesson, LessonScope, LessonType, LessonScopeEnum
│   │   │   │   ├── internal/                    # LessonService, LessonStudentsService, repos, mapper, specs
│   │   │   │   └── web/
│   │   │   │       ├── LessonController.java
│   │   │   │       ├── filters/
│   │   │   │       ├── requests/
│   │   │   │       └── responses/
│   │   │   ├── student/                         # Student domain module
│   │   │   │   ├── StudentApi.java
│   │   │   │   ├── domain/                      # Student
│   │   │   │   └── internal/
│   │   │   │       ├── StudentService.java
│   │   │   │       ├── StudentRepository.java
│   │   │   │       ├── StudentMapper.java
│   │   │   │       └── web/                     # NOTE: web DTOs inside internal/ (inconsistent)
│   │   │   │           ├── requests/            # CreateStudentRequest, UpdateStudentRequest
│   │   │   │           └── response/            # StudentResponse
│   │   │   ├── subject/                         # Subject domain module
│   │   │   │   ├── SubjectsApi.java
│   │   │   │   ├── TeacherSubjectPermissionsApi.java
│   │   │   │   ├── SubjectReferenceService.java # Cross-module reference interface
│   │   │   │   ├── domain/                      # Subject, TeacherSubjectPermission, PermissionScope, enums
│   │   │   │   ├── internal/                    # Services, repositories, mappers, specs
│   │   │   │   └── web/
│   │   │   │       ├── SubjectsController.java
│   │   │   │       ├── TeacherSubjectPermissionsController.java
│   │   │   │       ├── filters/
│   │   │   │       ├── requests/
│   │   │   │       └── responses/
│   │   │   └── teacher/                         # Teacher domain module
│   │   │       ├── TeachersApi.java
│   │   │       ├── TeacherReferenceService.java # Cross-module reference interface
│   │   │       ├── domain/                      # Teacher
│   │   │       ├── internal/                    # TeacherService, repo, mapper, spec
│   │   │       └── web/
│   │   │           ├── TeachersController.java
│   │   │           ├── filters/
│   │   │           ├── requests/
│   │   │           └── response/                # Note: "response" not "responses"
│   │   └── resources/
│   │       ├── application.yaml                 # Base config (port, JPA, OAuth2, CORS)
│   │       ├── application-dev.yaml             # Dev profile overrides
│   │       ├── application-prod.yaml            # Prod profile overrides
│   │       └── db/changelog/
│   │           ├── db.changelog-master.yaml     # Liquibase master file
│   │           └── v0/
│   │               ├── changelog.yaml           # Ordered include list
│   │               └── *.sql                    # Timestamped migration scripts
│   └── test/
│       ├── java/com/github/k1mb1/vkr_backend/  # Test classes
│       └── resources/                           # Test config
├── pom.xml                                      # Maven build descriptor
├── Dockerfile                                   # Container image definition
├── docker-compose.yaml                          # Local dev stack
├── lombok.config                                # Lombok settings
└── mvnw / mvnw.cmd                              # Maven wrapper
```

## Directory Purposes

**`common/domain/`:**
- Purpose: Shared JPA base classes used by every domain entity
- Contains: `Auditable`, `BaseEntity`, `ArchivableEntity`
- Key files: `src/main/java/com/github/k1mb1/vkr_backend/common/domain/BaseEntity.java`

**`common/error/`:**
- Purpose: Structured error model and centralised exception-to-HTTP mapping
- Contains: `GlobalExceptionHandler`, `ErrorDto`, `ErrorCode`, `FieldError`, `ErrorMessages`
- Key files: `src/main/java/com/github/k1mb1/vkr_backend/common/error/GlobalExceptionHandler.java`

**`common/security/`:**
- Purpose: Runtime JWT helpers for use in method security expressions
- Contains: `SecurityService` (registered as `@Component("securityService")`)
- Key files: `src/main/java/com/github/k1mb1/vkr_backend/common/security/SecurityService.java`

**`configs/`:**
- Purpose: Spring infrastructure configuration beans
- Contains: `SecurityConfig`, `JwtAuthConverter`, `JpaAuditingConfig`, `OpenApiConfig`
- Key files: `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java`

**`{module}/domain/`:**
- Purpose: JPA entity definitions and enums for the domain module
- Contains: `@Entity` classes extending `BaseEntity` or `ArchivableEntity`, domain enums

**`{module}/internal/`:**
- Purpose: Private implementation of the module — services, repositories, mappers, specifications
- Contains: `@Service` classes (package-private), `*Repository` interfaces, `@Mapper` MapStruct interfaces, `*Specifications` classes

**`{module}/web/`:**
- Purpose: HTTP layer for the module
- Contains: `@RestController`, filter beans (`@ModelAttribute`), request records, response records

**`{module}/*Api.java` (module root):**
- Purpose: Public interface that controllers call and services implement
- Contains: Method signatures with request/response types

**`db/changelog/v0/`:**
- Purpose: Liquibase migration SQL scripts, applied in order defined by `changelog.yaml`
- Generated: No — hand-written SQL
- Committed: Yes

## Key File Locations

**Entry Points:**
- `src/main/java/com/github/k1mb1/vkr_backend/VkrBackendApplication.java`: Spring Boot main class

**Configuration:**
- `src/main/resources/application.yaml`: Port, datasource, JPA, OAuth2 issuer URI, CORS
- `src/main/resources/application-dev.yaml`: Dev overrides
- `src/main/resources/application-prod.yaml`: Prod overrides
- `src/main/resources/db/changelog/db.changelog-master.yaml`: Liquibase root
- `pom.xml`: Maven dependencies and build plugins

**Core Business Logic:**
- `src/main/java/com/github/k1mb1/vkr_backend/attendance/internal/AttendanceService.java`
- `src/main/java/com/github/k1mb1/vkr_backend/attendance/checkin/internal/CheckInSessionService.java`
- `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonService.java`
- `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonStudentsService.java`
- `src/main/java/com/github/k1mb1/vkr_backend/group/internal/GroupService.java`

**Security:**
- `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java`
- `src/main/java/com/github/k1mb1/vkr_backend/configs/JwtAuthConverter.java`

**Error Handling:**
- `src/main/java/com/github/k1mb1/vkr_backend/common/error/GlobalExceptionHandler.java`

**Testing:**
- `src/test/java/com/github/k1mb1/vkr_backend/`: Test class root (currently sparse)
- `src/test/resources/`: Test application config

## Naming Conventions

**Files:**
- Domain entities: `PascalCase`, noun — e.g., `Attendance.java`, `CheckInSession.java`
- Enums: `PascalCase` with descriptive suffix — e.g., `AttendanceStatus.java`, `CheckInSessionState.java`
- Services: `PascalCase` + `Service` suffix — e.g., `AttendanceService.java`
- Repositories: `PascalCase` + `Repository` suffix — e.g., `AttendanceRepository.java`
- Mappers: `PascalCase` + `Mapper` suffix — e.g., `AttendanceMapper.java`
- Controllers: `PascalCase` + `Controller` suffix — e.g., `AttendanceController.java`
- Public API interfaces: `PascalCase` + `Api` suffix — e.g., `AttendanceApi.java`
- Reference service interfaces: `PascalCase` + `ReferenceService` suffix
- Filter objects: `PascalCase` + `Filter` suffix — e.g., `AttendanceFilter.java`
- Request records: descriptive verb + noun + `Request` — e.g., `UpsertAttendanceRequest.java`, `StartCheckInRequest.java`
- Response records: noun + `Response` suffix — e.g., `AttendanceTableResponse.java`

**Directories:**
- Module packages: lowercase singular nouns matching domain — `attendance`, `lesson`, `group`, `student`, `subject`, `teacher`
- Sub-layers: `domain`, `internal`, `web`
- HTTP sub-dirs: `filters`, `requests`, `responses` (inconsistency: `group` and `teacher` use `response` singular)

**Java packages:**
- Base: `com.github.k1mb1.vkr_backend`
- Module: `com.github.k1mb1.vkr_backend.{module}`
- Layer: `com.github.k1mb1.vkr_backend.{module}.{layer}`

## Where to Add New Code

**New domain module (e.g., `schedule`):**
1. Create package `com.github.k1mb1.vkr_backend.schedule`
2. Add entity in `schedule/domain/Schedule.java` extending `BaseEntity` or `ArchivableEntity`
3. Add public API interface `schedule/ScheduleApi.java`
4. Add `schedule/internal/ScheduleRepository.java` (Spring Data JPA)
5. Add `schedule/internal/ScheduleMapper.java` (MapStruct `@Mapper(componentModel = "spring")`)
6. Add `schedule/internal/ScheduleService.java` implementing `ScheduleApi`
7. Add `schedule/web/ScheduleController.java` injecting `ScheduleApi`
8. Add request/response records in `schedule/web/requests/` and `schedule/web/responses/`
9. Add Liquibase migration SQL in `src/main/resources/db/changelog/v0/YYYYMMDDHHMMSS-schedule.sql`
10. Register it in `src/main/resources/db/changelog/v0/changelog.yaml`

**New endpoint in existing module:**
- Add method to the `*Api` interface (module root)
- Implement in the `*Service` class (`internal/`)
- Add controller method in `*Controller` (`web/`)
- Add request/response records in `web/requests/` and `web/responses/`

**New cross-module reference access:**
- Define a `*ReferenceService` interface in the source module root
- Implement it in `*ReferenceServiceImpl` inside `internal/`
- Inject the interface (not the impl) in the consuming module's service

**New Liquibase migration:**
- Create `src/main/resources/db/changelog/v0/YYYYMMDDHHMMSS-description.sql`
- Add an `- include: file: db/changelog/v0/YYYYMMDDHHMMSS-description.sql` entry at the end of `src/main/resources/db/changelog/v0/changelog.yaml`

**New utility / shared component:**
- If shared across modules: place in `common/` with appropriate sub-package (`common/domain/`, `common/error/`, `common/security/`)
- If module-specific: place in `{module}/internal/`

## Special Directories

**`target/`:**
- Purpose: Maven build output, compiled classes, generated sources
- Generated: Yes — by `mvn compile` / `mvnw spring-boot:run`
- Committed: No (in `.gitignore`)

**`target/generated-sources/annotations/`:**
- Purpose: MapStruct-generated mapper implementations
- Generated: Yes — by annotation processor at compile time
- Committed: No

**`.planning/`:**
- Purpose: GSD planning documents (phases, codebase maps)
- Generated: By GSD tooling
- Committed: Yes

**`.claude/`:**
- Purpose: Claude agent configuration, GSD workflow scripts
- Committed: Yes

---

*Structure analysis: 2026-05-19*
