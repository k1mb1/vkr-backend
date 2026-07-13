package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Conventions for the JPA {@code Specification} layer. Specifications assemble
 * dynamic queries from a {@code *Filter} DTO and hand the resulting
 * {@code Specification} to the repository, so they are the one place that knows
 * both the domain entity and the filter shape. These rules keep that layer
 * narrow: {@code *Specification(s)} types in {@code ..specification..}, driven
 * only by services, that touch the domain and filter DTOs but never the
 * repository, controllers or web.
 */
@AnalyzeProductionClasses
class SpecificationRules {

    @ArchTest
    static final ArchRule specifications_are_named = classes()
            .that()
            .resideInAPackage(Packages.SPECIFICATION)
            .and()
            .doNotHaveSimpleName("package-info")
            .should()
            .haveSimpleNameEndingWith("Specification")
            .orShould()
            .haveSimpleNameEndingWith("Specifications")
            .allowEmptyShould(true)
            .because("query builders in ..specification.. are named *Specification(s)");

    @ArchTest
    static final ArchRule specification_types_reside_in_their_layer = classes()
            .that()
            .haveSimpleNameEndingWith("Specification")
            .or()
            .haveSimpleNameEndingWith("Specifications")
            .should()
            .resideInAPackage(Packages.SPECIFICATION)
            .allowEmptyShould(true)
            .because("a *Specification(s) is the query-building layer and belongs in ..specification..");

    @ArchTest
    static final ArchRule specifications_do_not_reach_into_other_layers = noClasses()
            .that()
            .resideInAPackage(Packages.SPECIFICATION)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    Packages.REPOSITORY, Packages.CONTROLLER, Packages.MAPPER, Packages.SPRING_HTTP, Packages.SERVLET)
            .because("specifications build queries from the domain entity and a *Filter only: "
                    + "no repository, controller, mapper or web types");
}
