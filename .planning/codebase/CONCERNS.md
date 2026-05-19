# Codebase Concerns

**Analysis Date:** 2026-05-19

## Tech Debt

**Unenforced Authorization — All Endpoints Are Public:**
- Issue: `SecurityConfig` sets `.anyRequest().permitAll()` meaning every API endpoint is accessible without authentication. The OAuth2 resource server is configured and JWT conversion works, but zero authorization checks are enforced at the method or URL level. `@EnableMethodSecurity` is declared but no `@PreAuthorize` annotations exist anywhere in the codebase.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java` (line 43)
- Impact: Any unauthenticated caller can read and mutate all data (students, attendance, lessons, subjects). The `SecurityService.isSameUser()` helper exists but is never called.
- Fix approach: Replace `.anyRequest().permitAll()` with `.anyRequest().authenticated()`. Add `@PreAuthorize` annotations on sensitive service or controller methods. Wire `SecurityService` for owner checks.

**Stub `extractResourceRoles` Always Returns Empty Set:**
- Issue: `JwtAuthConverter.extractResourceRoles()` always returns `Collections.emptySet()`. No roles are ever extracted from JWT claims, so any RBAC enforcement added later would silently grant no authorities.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/configs/JwtAuthConverter.java` (line 41-45)
- Impact: Role-based rules cannot be applied until this is implemented.
- Fix approach: Extract Keycloak realm/resource roles from `jwt.getClaimAsMap("realm_access")` or `resource_access`, map them to `SimpleGrantedAuthority`.

**Swagger / OpenAPI Exposed Without Authentication:**
- Issue: `/swagger-ui/**` and `/v3/api-docs/**` are explicitly `permitAll()`. In production this exposes the full API schema to anyone.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java` (lines 26-28), `src/main/resources/application-prod.yaml`
- Impact: API surface is fully documented for unauthenticated attackers. `application-prod.yaml` disables Swagger UI but `api-docs` path is still served unless additionally gated.
- Fix approach: Conditionally expose Swagger behind a role check, or restrict docs in production entirely via `springdoc.api-docs.enabled: false` in prod profile (currently only swagger-ui is disabled).

**Log Statement Pasted as a Code Comment in Main Application Class:**
- Issue: A raw server log line starting with `//TODO` was copy-pasted into `VkrBackendApplication.java` as a comment, recording a `PATCH` method not supported error from March 2026.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/VkrBackendApplication.java` (line 13)
- Impact: Suggests a routing bug existed; it is unclear whether it was resolved. The comment pollutes the entry point and documents nothing actionable.
- Fix approach: Remove the comment. Investigate if any PATCH endpoint still has misconfigured HTTP method mapping.

**Inconsistent `EntityNotFoundException` Import Style:**
- Issue: `GroupService` uses the fully-qualified `new jakarta.persistence.EntityNotFoundException(...)` inline without an import, while all other service classes use a proper import statement.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/group/internal/GroupService.java` (lines 92, 135, 163, 176), `src/main/java/com/github/k1mb1/vkr_backend/student/internal/StudentService.java` (line 45)
- Impact: Low functional impact; increases noise during code review.
- Fix approach: Add `import jakarta.persistence.EntityNotFoundException;` to `GroupService` and `StudentService` and remove qualified names.

**`createLessonsByType` Sets `startedAt` to `LocalDate.now()` With No Input:**
- Issue: `LessonService.createLessonsByType()` creates all lessons with `startedAt = LocalDate.now()`. The `CreateLessonsByTypeRequest` has no date field. Lessons created by this endpoint have no meaningful scheduled date.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonService.java` (line 171), `src/main/java/com/github/k1mb1/vkr_backend/lesson/web/requests/CreateLessonsByTypeRequest.java`
- Impact: Attendance tables ordered by `startedAt` will cluster all lessons created this way on the creation date, breaking chronological ordering.
- Fix approach: Add an optional `startedAt` field to `CreateLessonsByTypeRequest`, or document that this endpoint is for lesson-count placeholder creation only.

## Known Bugs

**PATCH Method Not Supported (Recorded in TODO Comment):**
- Symptoms: A 2026-03-24 log line pasted into `VkrBackendApplication.java` records `HttpRequestMethodNotSupportedException: Request method 'PATCH' is not supported`.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/VkrBackendApplication.java` (line 13)
- Trigger: Unknown — the comment gives no endpoint path. `LessonController` does have a `@PatchMapping("/{id}")`.
- Workaround: None documented; the PATCH endpoint for lessons appears to be mapped correctly now.

## Security Considerations

**All REST Endpoints Are Unauthenticated:**
- Risk: Any actor on the network can create, read, update, or delete teachers, students, groups, subjects, and attendance records without any credentials.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java`
- Current mitigation: OAuth2 JWT resource server is configured and tokens are parsed, but authorization is not enforced.
- Recommendations: Enforce `.anyRequest().authenticated()` immediately. Apply method-level `@PreAuthorize` for data-ownership checks using `SecurityService.isSameUser()`.

**Public Check-In Endpoint Accepts Any Student UUID Without Identity Verification:**
- Risk: The `/api/check-in-sessions/public/{id}/check-in` endpoint accepts a `studentId` in the request body. Any caller who knows a valid `studentId` UUID can submit attendance on behalf of any student in the session audience.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/attendance/checkin/web/PublicCheckInController.java`, `src/main/java/com/github/k1mb1/vkr_backend/attendance/checkin/internal/CheckInSessionService.java` (lines 291-323)
- Current mitigation: Only students already in the lesson audience are accepted; the service checks scope membership.
- Recommendations: Consider a challenge/token mechanism (e.g., a student-specific one-time token embedded in the QR code) to prevent impersonation.

**CORS Allows Any Configured Origin With Credentials:**
- Risk: `CorsConfiguration.setAllowedHeaders(List.of("*"))` combined with `setAllowCredentials(true)` is a misconfiguration — the wildcard header is ignored when credentials are allowed, so Spring replaces it with reflecting the actual request `Origin`. Allowed origins are env-configured, reducing the blast radius, but the wildcard header configuration is misleading and fragile.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java` (lines 57-62)
- Current mitigation: Origins are env-restricted via `${CORS_ALLOWED_ORIGINS}`.
- Recommendations: Replace `List.of("*")` for `allowedHeaders` with an explicit list of required headers (e.g., `Authorization`, `Content-Type`).

**CSRF Disabled:**
- Risk: CSRF protection is disabled globally via `AbstractHttpConfigurer::disable`.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java` (line 43)
- Current mitigation: Stateless JWT auth is used, which limits CSRF attack surface for authenticated endpoints.
- Recommendations: Acceptable for a pure JWT API; document the decision explicitly.

## Performance Bottlenecks

**N+1 Queries in `AttendanceService.unionStudentsAcross()`:**
- Problem: `getAttendanceTable()` first fetches all lessons unbounded, then calls `lessonStudentsApi.studentsOf(lesson)` in a loop. Each call inside `LessonStudentsService.studentsOf()` executes 1–N `StudentRepository` queries depending on scope count. For a subject with 30 lessons each targeting 2 scopes, this is up to 60 DB round-trips before the attendance data is fetched.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/attendance/internal/AttendanceService.java` (lines 107-116), `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonStudentsService.java` (lines 24-48)
- Cause: `studentsOf()` issues per-scope repository queries inside a Java loop; there is no batch or JOIN strategy.
- Improvement path: Collect all `groupId`/`subgroupId` pairs from all lessons in a single pass, then fetch students in one `IN` query. Alternatively, add a `findByGroupIdInAndArchivedAtIsNull` repository method and batch-resolve all lessons at once.

**Unbounded Lesson List in `CheckInSessionService.listForPermission()`:**
- Problem: `listForPermission()` calls `lessonRepository.findAll(LessonSpecifications.forPermission(permission))` with no pagination, returning the full lesson history. For long-running subjects this can be a large result set.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/attendance/checkin/internal/CheckInSessionService.java` (lines 105-119)
- Cause: No `Pageable` parameter and no date-range filter on the lesson query.
- Improvement path: Add a date-range or limit parameter to the filter, or paginate the response.

**Unbounded Lesson List in `LessonService.getLessons()`:**
- Problem: `getLessons()` returns all lessons for a permission with no size cap. The response is a plain `List<LessonResponse>` with no pagination.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonService.java` (lines 105-123), `src/main/java/com/github/k1mb1/vkr_backend/lesson/web/LessonController.java` (lines 33-44)
- Cause: API was not designed with pagination for this endpoint.
- Improvement path: Add `Pageable` support or at minimum accept date-range filter parameters in `LessonFilter`.

**`replaceRoster` Issues Individual `archiveStudent`/`updateStudent`/`createStudent` Calls per Student:**
- Problem: `GroupService.replaceRoster()` iterates roster entries and calls `studentApi.archiveStudent()`, `studentApi.updateStudent()`, or `studentApi.createStudent()` individually for each student. A group of 30 students triggers up to 60 DB round-trips (archive old + create/update new).
- Files: `src/main/java/com/github/k1mb1/vkr_backend/group/internal/GroupService.java` (lines 108-156)
- Cause: The `StudentApi` interface only exposes single-entity methods; no bulk operation exists.
- Improvement path: Add `bulkArchive(List<UUID>)` and `bulkCreate(List<CreateStudentRequest>)` to `StudentApi`; use `saveAll` in the repository.

## Fragile Areas

**`CheckInSessionService.confirm()` — Non-Atomic Attendance Upsert Loop:**
- Files: `src/main/java/com/github/k1mb1/vkr_backend/attendance/checkin/internal/CheckInSessionService.java` (lines 152-223)
- Why fragile: `confirm()` iterates all students and calls `attendanceApi.upsert()` for each inside a single `@Transactional` method. If any upsert throws partway through (e.g., a constraint violation on one student), the entire transaction rolls back and the session is NOT marked confirmed, but the client receives a 400 error with no way to distinguish partial failure from a clean rejection.
- Safe modification: Ensure all validations (override student membership checks) happen before any `attendanceApi.upsert()` call is made. The existing code does validate overrides first, but the attendance write loop is not guarded against mid-loop errors.
- Test coverage: No unit or integration tests covering this method.

**`LessonSpecifications.forPermission()` — Dynamic Predicate Builder with No Test Coverage:**
- Files: `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonSpecifications.java`
- Why fragile: The JPA Criteria API predicate builder is complex (nested subqueries with OR branches per permission scope). The `query.getResultType() != Long.class` guard for `distinct` is a workaround for Hibernate count query interference. Any change risks breaking lesson visibility or count queries silently.
- Safe modification: Do not change predicate logic without adding @DataJpaTest integration tests that cover: `allPermissions=true`, scoped permission with multiple groups, scoped permission with subgroup restriction.
- Test coverage: Zero — only `contextLoads()` exists in the test suite.

**`LessonStudentsService.studentsOf()` — No Guard for Lazy Collections on Detached Lesson:**
- Files: `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonStudentsService.java` (lines 27-39)
- Why fragile: `studentsOf()` accesses `lesson.getSubject().getGroups()` and `lesson.getScopes()` which are lazy collections. If `lesson` is passed from a context where those collections were not eagerly fetched (e.g., from a `findAll(Specification, Sort)` call that does not JOIN FETCH scopes), a `LazyInitializationException` will be thrown at runtime. Currently the callers that use `findAll` for lesson lists do not fetch scopes, so this path is latent.
- Safe modification: Always call `lessonStudentsApi.studentsOf()` with a lesson loaded via `findByIdWithDetails()`, or document clearly that the passed `Lesson` must have scopes initialized.
- Test coverage: None.

## Scaling Limits

**Lesson List Endpoints:**
- Current capacity: Unbounded — all lessons for a subject are returned in memory.
- Limit: Subjects with hundreds of lessons will produce large response payloads and high heap pressure during serialization.
- Scaling path: Paginate `getLessons` (`LessonController`) and `listForPermission` (`CheckInSessionController`) endpoints.

**Attendance Table Build:**
- Current capacity: `buildTable()` loads all attendance records for all lesson/student combinations into a `List<Attendance>` in memory before building the table response.
- Limit: A subject with 100 lessons and 100 students produces up to 10,000 `Attendance` rows loaded at once.
- Scaling path: Stream or paginate attendance records; consider returning a sparse cell map rather than a full matrix.

## Dependencies at Risk

**Spring Boot 4.0.2 (Pre-GA / Non-LTS):**
- Risk: `pom.xml` depends on `spring-boot-starter-parent` version `4.0.2`. Spring Boot 4.x is based on Spring Framework 7 and Jakarta EE 11 and was released very recently (2025). Community ecosystem, third-party library compatibility, and production hardening lag behind Spring Boot 3.x.
- Files: `pom.xml` (line 8)
- Impact: `springdoc-openapi` 3.0.2 may have incomplete compatibility; `org.mapstruct` 1.6.3 compatibility with Spring Boot 4.x annotation processor changes is unverified.
- Migration plan: Monitor Spring Boot 4.x release cadence; pin to a confirmed stable GA release. Run full integration test suite after any minor version upgrade.

## Missing Critical Features

**No Authorization Enforcement:**
- Problem: As described in Security Considerations — no endpoint requires a valid authenticated user. The authorization infrastructure (Spring Security, `@EnableMethodSecurity`, `SecurityService`) is in place but wired to do nothing.
- Blocks: Cannot deploy to a production environment with real user data without addressing this.

**No Integration or Unit Tests:**
- Problem: The only test file is `VkrBackendApplicationTests` containing a single `contextLoads()` smoke test. No service, repository, or controller layer has any coverage.
- Files: `src/test/java/com/github/k1mb1/vkr_backend/VkrBackendApplicationTests.java`
- Blocks: Any refactoring of business logic (especially `LessonSpecifications`, `CheckInSessionService.confirm()`, `GroupService.replaceRoster()`) carries undetectable regression risk.

**No Pagination on Lesson and Check-In Session List Endpoints:**
- Problem: `GET /api/lessons` and `GET /api/check-in-sessions` return unbounded lists.
- Blocks: Production use with large semester datasets.

## Test Coverage Gaps

**Business Logic — Zero Coverage:**
- What's not tested: All service classes — `CheckInSessionService`, `LessonService`, `AttendanceService`, `GroupService`, `TeacherSubjectPermissionService`, `SubjectService`, `StudentService`.
- Files: All files under `src/main/java/com/github/k1mb1/vkr_backend/*/internal/`
- Risk: Regressions in check-in session state machine, lesson scope resolution, roster replacement, and attendance upsert are invisible.
- Priority: High

**JPA Specifications — Zero Coverage:**
- What's not tested: `LessonSpecifications.forPermission()`, `LessonSpecifications.forPermissionScope()`, `SubjectSpecifications`, `GroupSpecifications`, `TeacherSpecification`.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/lesson/internal/LessonSpecifications.java`, `src/main/java/com/github/k1mb1/vkr_backend/subject/internal/SubjectSpecifications.java`
- Risk: Incorrect lesson visibility (showing or hiding lessons that should not be) is a silent data-integrity bug.
- Priority: High

**Security Configuration — Zero Coverage:**
- What's not tested: CORS configuration, JWT converter, any endpoint authorization behavior.
- Files: `src/main/java/com/github/k1mb1/vkr_backend/configs/SecurityConfig.java`, `src/main/java/com/github/k1mb1/vkr_backend/configs/JwtAuthConverter.java`
- Risk: Security regressions go undetected.
- Priority: High (once authorization enforcement is added)

---

*Concerns audit: 2026-05-19*
