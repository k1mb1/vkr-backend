# Coding Conventions

**Analysis Date:** 2026-05-19

## Naming Patterns

**Files:**
- Java classes: `PascalCase` matching the class name exactly (`GroupService.java`, `CheckInSessionController.java`)
- Interfaces used as public API contracts: `PascalCase` with `Api` suffix (`GroupsApi`, `CheckInSessionApi`, `LessonStudentsApi`)
- Mapper interfaces: `PascalCase` with `Mapper` suffix (`GroupMapper`, `LessonMapper`, `CheckInSessionMapper`)
- Repository interfaces: `PascalCase` with `Repository` suffix (`GroupRepository`, `CheckInSessionRepository`)
- Specification records: `PascalCase` with `Specifications` suffix (`GroupSpecifications`, `LessonSpecifications`)
- Filter records (query param objects): `PascalCase` with `Filter` suffix (`GroupFilter`, `LessonFilter`, `TeacherFilter`)
- Request records: `PascalCase` with `Request` suffix (`CreateGroupRequest`, `UpdateLessonRequest`)
- Response records: `PascalCase` with `Response` suffix (`GroupResponse`, `GroupPageResponse`)
- Config classes: `PascalCase` with `Config` suffix (`SecurityConfig`, `JpaAuditingConfig`)

**Packages:**
- Module root: `com.github.k1mb1.vkr_backend.<module>` (e.g., `group`, `lesson`, `attendance`)
- Public API interface: placed directly in the module root package
- Implementation classes (hidden from outside): placed in `<module>.internal`
- Web layer: placed in `<module>.web`, with sub-packages `requests/`, `responses/`, `filters/`
- Domain entities: placed in `<module>.domain`

**Methods:**
- camelCase throughout
- CRUD methods follow: `create*`, `update*`, `get*ById`, `getPage`/`getAll`, `delete*`
- Repository custom queries: `findWithDetailsById` (for `@EntityGraph` eager-fetch variants), `findBy<Condition>`

**Variables:**
- camelCase (`var` inference used pervasively in method bodies)
- Plural names for collections: `subgroups`, `lessonIds`, `students`

**Constants:**
- `UPPER_SNAKE_CASE` in `final` fields: `PUBLIC_ENDPOINTS`, `NOT_FOUND_MESSAGE`, `VALIDATION_FAILED`

**Types:**
- Entities: `PascalCase` JPA classes extending `BaseEntity`, `ArchivableEntity`, or `Auditable`
- DTOs / transfer objects: Java `record` types with `@Builder` for construction
- Enums: `PascalCase` class, `UPPER_SNAKE_CASE` values (`AttendanceStatus.ABSENT`, `CheckInSessionState.OPEN`)

## Code Style

**Formatting:**
- No explicit formatter config detected (no `.editorconfig`, no Checkstyle, no Google Java Format config)
- Observed: 4-space indentation, opening brace on same line, closing brace on own line
- Line length: approximately 100 characters before wrapping (inferred from observed code)
- Chained stream calls: each step on a new line, indented 4 spaces from the chain start

**Lombok usage:**
- `@Getter`, `@Setter` on all entities and DTOs
- `@RequiredArgsConstructor` on all `@Service` and `@RestController` classes
- `@SuperBuilder(toBuilder = true)` on all entity hierarchy classes (required for `@MappedSuperclass` chains)
- `@NoArgsConstructor`, `@AllArgsConstructor` on entities
- `@Builder` on record DTOs and request/response types
- `@Slf4j` on `GlobalExceptionHandler` and services that log

**Static imports:**
- `HttpStatus.*` in `GlobalExceptionHandler` (e.g., `NOT_FOUND`, `BAD_REQUEST`)
- `ErrorMessages.*` constants in `GlobalExceptionHandler`
- `org.mapstruct.MappingConstants.ComponentModel.SPRING` and `NullValuePropertyMappingStrategy.IGNORE` in mappers

## Import Organization

**Order (observed in service files):**
1. Project-internal imports (`com.github.k1mb1.vkr_backend.*`) — alphabetically sorted within each module group
2. `java.*` standard library imports
3. Framework imports (`lombok.*`, `org.springframework.*`)

Note: No automatic import sorting enforced by tooling; ordering is manually maintained.

**No path aliases** — standard Java fully-qualified imports only.

## Error Handling

**Centralized handler:** `GlobalExceptionHandler` at `src/main/java/com/github/k1mb1/vkr_backend/common/error/GlobalExceptionHandler.java`
annotated with `@RestControllerAdvice`.

**Exception-to-response mapping:**
- `EntityNotFoundException` → `404 NOT_FOUND`
- `MethodArgumentNotValidException` → `400 VALIDATION_FAILED` with `FieldError` list
- `ConstraintViolationException` → `400 VALIDATION_FAILED` with `FieldError` list
- `IllegalArgumentException` → `400 ILLEGAL_ARGUMENT`
- `IllegalStateException` → `400 ILLEGAL_STATE`
- `HttpMessageNotReadableException` → `400 MALFORMED_BODY`
- `MethodArgumentTypeMismatchException` → `400 INVALID_PARAM`
- `AccessDeniedException` → `403 ACCESS_DENIED`
- `NoResourceFoundException` → `404 NOT_FOUND`
- `DataIntegrityViolationException` → `409 CONFLICT`
- `Exception` (catch-all) → `500 INTERNAL_ERROR`

**Error response shape:** `ErrorDto` record at `src/main/java/com/github/k1mb1/vkr_backend/common/error/ErrorDto.java`
```java
record ErrorDto(int status, ErrorCode code, String message, Instant timestamp, List<FieldError> fieldErrors)
```

**Throwing pattern in services:**
- Use `EntityNotFoundException` (jakarta.persistence) for missing entities — thrown inline with lambda in `orElseThrow`:
  ```java
  .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id))
  ```
- Use `IllegalStateException` for business rule violations (e.g., session already confirmed):
  ```java
  throw new IllegalStateException("Session already confirmed: " + sessionId);
  ```
- Use `IllegalArgumentException` for invalid input combinations:
  ```java
  throw new IllegalArgumentException("scopes must be non-empty when allGroups=false");
  ```
- Never throw checked exceptions from service methods; all exceptions are unchecked.

**Error codes:** centralized in `ErrorCode` enum at `src/main/java/com/github/k1mb1/vkr_backend/common/error/ErrorCode.java`

**Error messages:** centralized string constants in `ErrorMessages` at `src/main/java/com/github/k1mb1/vkr_backend/common/error/ErrorMessages.java`

## Logging

**Framework:** SLF4J via Lombok `@Slf4j`

**Patterns:**
- `log.warn(...)` in `GlobalExceptionHandler` for client errors (4xx): entity not found, illegal argument/state, access denied, unreadable body
- `log.error("Unexpected error", ex)` for unhandled server errors (5xx) — full stack trace included
- No application-level business logging observed in service classes (logging is centralized to the exception handler)

## Comments

**When to Comment:**
- Inline comments explain non-obvious decisions (e.g., `// first check-in wins; do not downgrade PRESENT to LATE on repeated submission` in `CheckInSessionService`)
- Inline Russian-language comments appear in config code (e.g., in `SecurityConfig`): `// Все заголовки включая Authorization`
- TODO comments placed inline in source (one observed in `VkrBackendApplication.java`)

**JSDoc/TSDoc:**
- OpenAPI documentation is provided via SpringDoc annotations (`@Operation`, `@Tag`, `@Parameter`, `@Schema`) — not Javadoc
- No Javadoc blocks observed on any class or method

## Function Design

**Size:** Service methods are moderate in size. Complex orchestration methods (e.g., `confirm`, `createGroup`, `bulkScheduleLessons`) can reach 30–60 lines; they extract private helper methods (`loadSession`, `recordsByStudentId`, `replaceRoster`, `resolveScopes`, `buildLessonScopes`).

**Parameters:** Prefer value objects (request records) over multiple primitive parameters for public API methods. Private helpers use positional primitives.

**Return Values:**
- Service methods return response records directly (never `Optional` — unwrap in service layer, throw on absent)
- `void` return for delete operations
- `List<T>` for bulk results, `Page<T>` for paginated queries
- `ResponseEntity<T>` used in all controller methods

## Module Design

**Public API interfaces:** Each module exposes a public interface (e.g., `GroupsApi`, `LessonApi`, `AttendanceApi`) placed at the module root package. Implementation classes are in the `internal` sub-package and are package-private where possible.

**Service visibility:**
- Service implementation classes (e.g., `GroupService`, `LessonService`) are package-private (`class`, not `public class`), hidden behind the public API interface.
- Mapper interfaces are also package-private within `internal`.

**Repository pattern:** All repositories extend `JpaRepository<Entity, UUID>` and `JpaSpecificationExecutor<Entity>` (when filtering is needed). Custom fetch methods named `findWithDetailsById` use `@EntityGraph` for eager loading.

**Transaction pattern:**
- `@Transactional(readOnly = true)` at class level on all service classes
- `@Transactional` (read-write) annotated on individual mutating methods

**MapStruct mappers:** All entity-to-response mapping is done via MapStruct `@Mapper(componentModel = SPRING)` interfaces. `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` used for partial updates.

**Specifications pattern:** Query filtering uses Spring Data JPA `Specification<T>` built in package-private records named `<Module>Specifications` (e.g., `GroupSpecifications`, `LessonSpecifications`). Filter parameters are passed via `*Filter` record objects.

**Soft delete:** Entities that support archiving extend `ArchivableEntity` at `src/main/java/com/github/k1mb1/vkr_backend/common/domain/ArchivableEntity.java`. Soft-deleted rows are filtered globally via `@SQLRestriction("archived_at IS NULL")` on entity classes.

**OpenAPI documentation:** All controllers and DTOs are annotated with SpringDoc annotations. Controllers use `@Tag`, `@Operation`, `@Parameter`; DTOs use `@Schema`. Internal/base classes are hidden from API docs via `@Hidden`.

---

*Convention analysis: 2026-05-19*
