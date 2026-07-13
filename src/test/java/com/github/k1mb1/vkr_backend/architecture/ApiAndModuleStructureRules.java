package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.NamedInterface;

/**
 * Module structure and the shape of the published cross-module API. These rules
 * keep the only inter-module surface ({@code ..api..}) narrow and self-contained,
 * and keep the module declarations themselves honest (explicit dependencies, no
 * wholesale opening, no cycles).
 */
@AnalyzeProductionClasses
class ApiAndModuleStructureRules {

    // --- Published API surface ----------------------------------------------

    @ArchTest
    static final ArchRule api_is_self_contained = noClasses()
            .that()
            .resideInAPackage(Packages.API)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.DOMAIN, Packages.REPOSITORY, Packages.SERVICE)
            .because("the published api must not drag in a module's internals; "
                    + "it is the only surface other modules may use");

    @ArchTest
    static final ArchRule api_exposes_only_ports_and_dtos = classes()
            .that()
            .resideInAPackage(Packages.API)
            .and()
            .doNotHaveSimpleName("package-info")
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
            .because("published types must be reachable by consuming modules");

    // --- Module declarations ------------------------------------------------

    @ArchTest
    static final ArchRule module_package_infos_declare_a_module_or_named_interface = classes()
            .that()
            .haveSimpleName("package-info")
            .and()
            .resideOutsideOfPackage(Packages.ROOT)
            .should()
            .beAnnotatedWith(ApplicationModule.class)
            .orShould()
            .beAnnotatedWith(NamedInterface.class)
            .allowEmptyShould(true)
            .because("a module root declares @ApplicationModule; a published api package declares @NamedInterface");

    @ArchTest
    static final ArchRule business_modules_declare_explicit_dependencies = classes()
            .that()
            .areAnnotatedWith(ApplicationModule.class)
            .should(ArchConditions.declareExplicitAllowedDependencies())
            .allowEmptyShould(true)
            .because("the module graph stays a reviewed contract; only the OPEN common module is exempt");

    @ArchTest
    static final ArchRule only_common_may_be_an_open_module = classes()
            .that()
            .areAnnotatedWith(ApplicationModule.class)
            .should(ArchConditions.beOpenOnlyInCommon())
            .allowEmptyShould(true)
            .because("business modules publish a named interface instead of disabling encapsulation wholesale");

    @ArchTest
    static final ArchRule modules_are_free_of_cycles = slices().matching(Packages.ROOT + ".(*)..")
            .should()
            .beFreeOfCycles()
            .because("a dependency cycle between modules makes them impossible to reason about in isolation");
}
