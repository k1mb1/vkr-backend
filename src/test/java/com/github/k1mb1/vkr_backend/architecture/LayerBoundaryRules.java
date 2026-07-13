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
    // Pins the documented graph loans -> catalog -> common as an explicit
    // contract (Modulith verifies declared dependencies; these forbid the reverse
    // edges outright, so a cycle can never be introduced by widening a declaration).

    @ArchTest
    static final ArchRule catalog_does_not_depend_on_loans = noClasses()
            .that()
            .resideInAPackage(Packages.ROOT + ".catalog..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(Packages.ROOT + ".loans..")
            .because("the module graph is one-directional: loans -> catalog, never the reverse");

    @ArchTest
    static final ArchRule common_depends_on_no_business_module = noClasses()
            .that()
            .resideInAPackage(Packages.COMMON)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.ROOT + ".catalog..", Packages.ROOT + ".loans..")
            .because("common is a leaf module; business modules depend on it, never the reverse");
}
