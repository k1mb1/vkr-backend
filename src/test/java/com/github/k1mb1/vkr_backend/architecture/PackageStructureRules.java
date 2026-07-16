package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Feature-package structure and the shape of the published cross-package surface.
 * These rules keep the only inter-package surface ({@code ..api..}) narrow and
 * self-contained, and keep the package graph acyclic — the guarantees the
 * aggregate layout relies on once Spring Modulith no longer enforces them.
 */
@AnalyzeProductionClasses
class PackageStructureRules {

    // --- The package graph is acyclic ---------------------------------------

    @ArchTest
    static final ArchRule packages_are_free_of_cycles = slices().matching(Packages.ROOT + ".(*)..")
            .should()
            .beFreeOfCycles()
            .because("a dependency cycle between feature packages makes them impossible to reason about "
                    + "in isolation (the whole point of the aggregate boundaries)");

    // --- Published api surface ----------------------------------------------

    @ArchTest
    static final ArchRule api_is_self_contained = noClasses()
            .that()
            .resideInAPackage(Packages.API)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.DOMAIN, Packages.REPOSITORY, Packages.SERVICE)
            .because("the published api must not drag in a package's internals; "
                    + "it is the only surface other packages may use");

    @ArchTest
    static final ArchRule api_exposes_only_ports_and_dtos = classes()
            .that()
            .resideInAPackage(Packages.API)
            .and()
            .doNotHaveSimpleName("package-info")
            .and()
            // nested helper types (an enum inside a DTO record) are namespaced by their parent
            .areTopLevelClasses()
            .should()
            .beInterfaces()
            .orShould()
            .beRecords()
            .because("only port interfaces and DTO records belong in a published api package");

    @ArchTest
    static final ArchRule api_types_are_public = classes()
            .that()
            .resideInAPackage(Packages.API)
            .and()
            .doNotHaveSimpleName("package-info")
            .should()
            .bePublic()
            .because("published types must be reachable by consuming packages");
}
