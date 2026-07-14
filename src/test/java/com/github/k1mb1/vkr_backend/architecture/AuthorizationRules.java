package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Method-security conventions. Authorization is a use-case boundary drawn with
 * {@code @PreAuthorize} in the service layer, and — like {@code @Transactional} —
 * it rides on Spring's proxy, so the same silent-no-op traps apply: a
 * {@code @PreAuthorize} the proxy never sees leaves an endpoint wide open.
 */
@AnalyzeProductionClasses
class AuthorizationRules {

    @ArchTest
    static final ArchRule pre_authorize_methods_are_public = methods()
            .that()
            .areAnnotatedWith(PreAuthorize.class)
            .should()
            .bePublic()
            .because("Spring Security's method interceptor only advises public methods; "
                    + "@PreAuthorize on a non-public method silently does nothing and leaves the call unguarded");

    @ArchTest
    static final ArchRule pre_authorize_lives_in_the_service_layer = methods()
            .that()
            .areAnnotatedWith(PreAuthorize.class)
            .should()
            .beDeclaredInClassesThat()
            .resideInAPackage(Packages.SERVICE)
            .andShould()
            .beDeclaredInClassesThat()
            .resideOutsideOfPackage(Packages.DTO)
            .because("authorization is a use-case boundary: @PreAuthorize belongs on service methods, "
                    + "not on controllers (which would guard transport, not the use case) or DTOs");
}
