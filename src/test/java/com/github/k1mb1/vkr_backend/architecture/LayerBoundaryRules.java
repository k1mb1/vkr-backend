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
 * persistence or leak an entity across a transport boundary. Each rule pins one
 * boundary that no compiler or formatter can see.
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
            // package-info carries package documentation, not domain logic
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

    // --- common stays technical ---------------------------------------------

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
            .because("common is the shared technical package: technical base types only, "
                    + "no web/business/persisted-entity leaks");

    // --- Cross-package encapsulation ----------------------------------------

    @ArchTest
    static final ArchRule services_use_only_their_own_packages_repositories = classes()
            .that()
            .resideInAPackage(Packages.SERVICE)
            .should(ArchConditions.dependOnRepositoriesOfOwnPackageOnly())
            .because("another package's data is reached through its published port or your own read-only "
                    + "ref repository, never its repository directly");

    @ArchTest
    static final ArchRule foreign_entity_repositories_are_read_only = classes()
            .that()
            .resideInAPackage(Packages.REPOSITORY)
            .and()
            .areInterfaces()
            .should(ArchConditions.keepForeignEntityRepositoriesReadOnly())
            .because("another package's aggregate is mutated only by its owner: a consumer's repository over a "
                    + "foreign entity is a read-only view (finders + getReferenceById), so it must "
                    + "extend Repository, never CrudRepository/JpaRepository");

    // --- Feature-package dependency graph is one-directional ----------------
    // Pins the documented order (see ARCHITECTURE.md) as an explicit contract; each
    // rule forbids the reverse edges outright, so a cycle can never be introduced:
    //
    //   journal -> lesson -> teacher -> subject -> group
    //   journal, lesson -> teacher (visibility under a permission)
    //   auth   -> (nothing but common); every business package may use auth
    //   common -> (leaf)

    @ArchTest
    static final ArchRule group_is_a_leaf_reference_package = noClasses()
            .that()
            .resideInAPackage(Packages.PKG_GROUP)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.PKG_SUBJECT, Packages.PKG_TEACHER, Packages.PKG_LESSON, Packages.PKG_JOURNAL)
            .because("group (контингент) is reference data at the bottom of the graph; "
                    + "everything else depends on it, never the reverse");

    @ArchTest
    static final ArchRule subject_depends_only_downward = noClasses()
            .that()
            .resideInAPackage(Packages.PKG_SUBJECT)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.PKG_TEACHER, Packages.PKG_LESSON, Packages.PKG_JOURNAL)
            .because("subject (предметы, политики) sits below teacher/lesson/journal: they depend on subject, "
                    + "never the reverse — the owner auto-grant and visibility filter are inverted through "
                    + "subject's own ports (subject.api)");

    @ArchTest
    static final ArchRule teacher_depends_only_downward = noClasses()
            .that()
            .resideInAPackage(Packages.PKG_TEACHER)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.PKG_LESSON, Packages.PKG_JOURNAL)
            .because("teacher (права) is consumed by lesson/journal for table visibility; "
                    + "it depends on subject/group below it, never on lesson/journal above");

    @ArchTest
    static final ArchRule lesson_does_not_depend_on_journal = noClasses()
            .that()
            .resideInAPackage(Packages.PKG_LESSON)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(Packages.PKG_JOURNAL)
            .because("lesson is the schedule core; the journal (marks) builds on it — "
                    + "the reverse direction is inverted through lesson's own ports (lesson.api)");

    @ArchTest
    static final ArchRule auth_depends_on_no_business_package = noClasses()
            .that()
            .resideInAPackage(Packages.PKG_AUTH)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.BUSINESS)
            .because("auth is infrastructure: business packages implement its ports (auth.api), "
                    + "so auth itself stays a leaf and can never join a cycle");

    @ArchTest
    static final ArchRule common_depends_on_no_business_or_auth_package = noClasses()
            .that()
            .resideInAPackage(Packages.COMMON)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.BUSINESS_AND_AUTH)
            .because("common is the leaf technical package; everything depends on it, never the reverse");
}
