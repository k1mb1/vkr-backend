# Testing Patterns

**Analysis Date:** 2026-05-19

## Test Framework

**Runner:**
- JUnit 5 (JUnit Jupiter) — via `spring-boot-starter-webmvc-test` / Spring Boot 4.0.2 test starters
- Config: no separate `junit-platform.properties` detected; Spring Boot auto-configures the runner

**Assertion Library:**
- Spring Boot Test assertions (JUnit 5 `Assertions`, `SpringBootTest`)

**Available test starters (pom.xml):**
- `spring-boot-starter-data-jpa-test` — slice testing for JPA repositories
- `spring-boot-starter-liquibase-test` — Liquibase test support
- `spring-boot-starter-validation-test` — Bean validation test support
- `spring-boot-starter-webmvc-test` — `MockMvc` / `@WebMvcTest` slice
- `spring-boot-starter-security-oauth2-resource-server-test` — JWT mock support for security tests

**Run Commands:**
```bash
./mvnw test                    # Run all tests
./mvnw test -Dtest=ClassName   # Run a single test class
./mvnw verify                  # Run tests + verify phase
```

## Test File Organization

**Location:**
- Test sources: `src/test/java/com/github/k1mb1/vkr_backend/`
- Test resources: `src/test/resources/`
- Mirror the main source package structure (co-located by convention, physically separate under `src/test`)

**Naming:**
- Test classes: `<ClassName>Tests.java` (Spring Boot convention, e.g., `VkrBackendApplicationTests.java`)

**Current test structure:**
```
src/test/
├── java/com/github/k1mb1/vkr_backend/
│   └── VkrBackendApplicationTests.java   # Application context smoke test
└── resources/
    └── application-test.yaml              # Test profile configuration
```

## Test Structure

**Suite Organization:**
```java
@SpringBootTest
class VkrBackendApplicationTests {

    @Test
    void contextLoads() {
        // verifies the Spring application context starts successfully
    }
}
```

**Patterns:**
- Application context smoke test uses `@SpringBootTest` with no additional configuration
- Test profile `application-test.yaml` provides overrides for JWT issuer URI and CORS origins
- Currently only one test class exists — the default generated smoke test

## Mocking

**Framework:** Spring Boot Test mock support (Mockito is available via spring-boot-starter-test transitive dependency)

**No mocking patterns currently in use** — the only test is a context load test with no mocks.

**Recommended patterns for new tests (based on available starters):**

Repository/JPA slice tests:
```java
@DataJpaTest
class GroupRepositoryTest {
    @Autowired
    GroupRepository groupRepository;

    @Test
    void findWithDetailsById_returnsGroupWithSubgroups() {
        // ...
    }
}
```

Controller slice tests with MockMvc and JWT mock:
```java
@WebMvcTest(GroupsController.class)
class GroupsControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean GroupsApi groupsApi;

    @Test
    @WithMockUser
    void getGroupById_returns200() throws Exception {
        // ...
    }
}
```

**What to Mock:**
- External service dependencies (`GroupsApi`, `AttendanceApi`, etc.) when testing controllers
- Security context via `@WithMockUser` or `@WithMockJwt` (oauth2 resource server test support)

**What NOT to Mock:**
- The database in `@DataJpaTest` — uses an embedded or Testcontainers database
- `GlobalExceptionHandler` — it is auto-loaded with `@WebMvcTest` and should not be mocked

## Fixtures and Factories

**Test Data:**
- No fixture classes or factory utilities exist currently
- Builder pattern is available on all request/response records (Lombok `@Builder`); use these directly in test setup:
  ```java
  var request = CreateGroupRequest.builder()
      .name("Test Group")
      .students(List.of(...))
      .build();
  ```

**Location:**
- No dedicated fixtures directory; place test helpers in `src/test/java/com/github/k1mb1/vkr_backend/` under a `support/` or `fixtures/` sub-package when introduced

## Coverage

**Requirements:** None enforced (no JaCoCo or Surefire coverage configuration in `pom.xml`)

**View Coverage:**
```bash
./mvnw test jacoco:report    # requires adding JaCoCo plugin to pom.xml first
```

## Test Types

**Unit Tests:**
- Not currently present (no isolated unit tests for service or mapper classes)
- Appropriate for: `GroupSpecifications`, `CheckInSession.stateAt()`, `CheckInSession.statusForCheckInAt()`, entity builder logic

**Integration Tests:**
- One application-level context test (`VkrBackendApplicationTests`) using `@SpringBootTest`
- Infrastructure available for repository slice tests (`@DataJpaTest`) and controller slice tests (`@WebMvcTest`)

**E2E Tests:**
- Not present

## Test Configuration

**Profile:** `application-test.yaml` at `src/test/resources/application-test.yaml` activates the `test` Spring profile and overrides:
- `spring.security.oauth2.resourceserver.jwt.issuer-uri: http://localhost:8080`
- `app.cors.allowed-origins: http://localhost:3000`

Datasource URL, username, and password must also be provided for integration tests (via `@DataJpaTest` embedded DB or Testcontainers).

## Coverage Gaps

**Everything except context load is untested.** Critical areas with no tests:

- **Service business logic** — `CheckInSessionService`, `GroupService`, `LessonService`, `AttendanceService`
- **State machine logic** — `CheckInSession.stateAt()` and `statusForCheckInAt()` are domain methods with branching logic and no unit tests
- **Repository custom queries** — `findWithDetailsById` with `@EntityGraph`, Specification-based filters
- **Exception handler** — `GlobalExceptionHandler` error response shapes not verified
- **Validation constraints** — `@NotBlank`, `@NotEmpty` on request records not exercised
- **Security** — role/ownership checks not tested (no `@PreAuthorize` present, but JWT converter and `SecurityService.isSameUser` are untested)
- **Mappers** — MapStruct-generated mapper implementations not verified for correctness

---

*Testing analysis: 2026-05-19*
