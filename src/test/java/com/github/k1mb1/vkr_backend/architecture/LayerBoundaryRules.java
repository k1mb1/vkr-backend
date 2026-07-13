package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * "No-leak" boundary rules: dependencies that the layering allows by direction
 * but that we still forbid because they would couple a transport type to
 * persistence, leak a module's internals, or wire the module graph the wrong
 * way. Each rule pins one boundary that no compiler or formatter can see.
 */
@AnalyzeProductionClasses
class LayerBoundaryRules {

    // --- Persistence must not leak across transport boundaries --------------

    @ArchTest
    static final ArchRule controllers_do_not_touch_persistence = noClasses()
            .that()
            .resideInAPackage(Packages.CONTROLLER)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.JPA, Packages.DOMAIN, Packages.REPOSITORY)
            .because("controllers work only with DTOs through services, never with entities or repositories");

    @ArchTest
    static final ArchRule dtos_do_not_depend_on_entities = noClasses()
            .that()
            .resideInAPackage(Packages.DTO)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(Packages.DOMAIN)
            .because("DTOs are transport types and must evolve independently of the domain");

    @ArchTest
    static final ArchRule repositories_do_not_depend_on_dtos = noClasses()
            .that()
            .resideInAPackage(Packages.REPOSITORY)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(Packages.DTO)
            .because("repositories speak entities; mapping to DTOs is the service's job");

    @ArchTest
    static final ArchRule transport_types_are_free_of_persistence = noClasses()
            .that()
            .resideInAnyPackage(Packages.DTO, Packages.API)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(Packages.JPA)
            .because("DTOs and the published API are transport types: no JPA may leak across them");

    @ArchTest
    static final ArchRule services_do_not_leak_web_types = noClasses()
            .that()
            .resideInAPackage(Packages.SERVICE)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.SPRING_HTTP, Packages.SERVLET)
            .because("web concerns (ResponseEntity, HttpStatus, servlet types) belong in controllers; "
                    + "the service layer speaks DTOs and domain types only");

    // --- Domain stays the core ----------------------------------------------

    @ArchTest
    static final ArchRule business_domain_stays_framework_agnostic = noClasses()
            .that()
            .resideInAPackage(Packages.DOMAIN)
            .and()
            .resideOutsideOfPackage(Packages.COMMON)
            .and()
            // package-info carries the Modulith @NamedInterface metadata, not domain logic
            .doNotHaveSimpleName("package-info")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(Packages.SPRING)
            .because("business entities map persistence (jakarta) but must not couple to Spring "
                    + "(common.domain is the shared technical base where Spring Data auditing is wired)");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_transport_types = noClasses()
            .that()
            .resideInAPackage(Packages.DOMAIN)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.DTO, Packages.API)
            .because("the domain is the core: DTOs/api depend on it, never the other way round");

    // --- Cross-module encapsulation -----------------------------------------

    @ArchTest
    static final ArchRule services_use_only_their_own_modules_repositories = classes()
            .that()
            .resideInAPackage(Packages.SERVICE)
            .should(ArchConditions.dependOnRepositoriesOfOwnModuleOnly())
            .because("another module's data is reachable only through its published api, never its repository");

    @ArchTest
    static final ArchRule common_stays_technical = noClasses()
            .that()
            .resideInAPackage(Packages.COMMON)
            .should()
            .beAnnotatedWith(RestController.class)
            .orShould()
            .beAnnotatedWith(Service.class)
            .orShould()
            .beAnnotatedWith(jakarta.persistence.Entity.class)
            .because("common is the shared OPEN module: technical base types only, "
                    + "no web/business/persisted-entity leaks");

    // --- Module graph is one-directional ------------------------------------
    // Pins the documented graph as an explicit contract (Modulith verifies the
    // declared dependencies; these rules forbid the reverse edges outright, so a
    // cycle can never be introduced by widening a declaration):
    //
    //   results -> grading -> attendance -> lesson -> subject -> {group, teacher}
    //   auth    -> (nothing but common); every business module may use auth
    //   common  -> (leaf, OPEN)

    private static final String[] BUSINESS_MODULES = {
        Packages.ROOT + ".teacher..",
        Packages.ROOT + ".group..",
        Packages.ROOT + ".subject..",
        Packages.ROOT + ".lesson..",
        Packages.ROOT + ".attendance..",
        Packages.ROOT + ".grading..",
        Packages.ROOT + ".results..",
    };

    @ArchTest
    static final ArchRule reference_modules_are_leaves = noClasses()
            .that()
            .resideInAnyPackage(Packages.ROOT + ".teacher..", Packages.ROOT + ".group..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    Packages.ROOT + ".subject..",
                    Packages.ROOT + ".lesson..",
                    Packages.ROOT + ".attendance..",
                    Packages.ROOT + ".grading..",
                    Packages.ROOT + ".results..")
            .because("teacher and group are reference data at the bottom of the graph; "
                    + "the teaching-process modules depend on them, never the reverse");

    @ArchTest
    static final ArchRule subject_depends_only_downward = noClasses()
            .that()
            .resideInAPackage(Packages.ROOT + ".subject..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    Packages.ROOT + ".lesson..",
                    Packages.ROOT + ".attendance..",
                    Packages.ROOT + ".grading..",
                    Packages.ROOT + ".results..")
            .because("subject (policies, permissions) sits below the lesson/marks modules: "
                    + "lesson -> subject, never the reverse");

    @ArchTest
    static final ArchRule lesson_depends_only_downward = noClasses()
            .that()
            .resideInAPackage(Packages.ROOT + ".lesson..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    Packages.ROOT + ".attendance..", Packages.ROOT + ".grading..", Packages.ROOT + ".results..")
            .because("lesson is the schedule core; the marks modules (attendance, grading) build on it — "
                    + "the reverse direction is inverted through lesson's own ports (lesson.api)");

    @ArchTest
    static final ArchRule attendance_does_not_depend_on_grading_or_results = noClasses()
            .that()
            .resideInAPackage(Packages.ROOT + ".attendance..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.ROOT + ".grading..", Packages.ROOT + ".results..")
            .because("grading composes attendance summaries, never the reverse; "
                    + "results is the top-level read-only aggregator");

    @ArchTest
    static final ArchRule auth_depends_on_no_business_module = noClasses()
            .that()
            .resideInAPackage(Packages.ROOT + ".auth..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(BUSINESS_MODULES)
            .because("auth is infrastructure: business modules implement its SPI ports (auth.api), "
                    + "so auth itself stays a leaf and can never join a module cycle");

    @ArchTest
    static final ArchRule common_depends_on_no_business_module = noClasses()
            .that()
            .resideInAPackage(Packages.COMMON)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(BUSINESS_MODULES)
            .because("common is a leaf module; business modules depend on it, never the reverse");
}
