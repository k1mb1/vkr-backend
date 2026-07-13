package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction-boundary conventions. Transactions are a service-layer concern; the
 * default is read-only and writes opt in per method. These rules also guard the
 * silent-no-op traps Spring's proxy-based {@code @Transactional} is prone to.
 */
@AnalyzeProductionClasses
class TransactionRules {

    @ArchTest
    static final ArchRule transactional_methods_live_in_the_service_layer = methods()
            .that()
            .areAnnotatedWith(Transactional.class)
            .should()
            .beDeclaredInClassesThat()
            .resideInAPackage(Packages.SERVICE)
            .because("transaction boundaries are drawn around use cases in the service layer");

    @ArchTest
    static final ArchRule transactional_classes_live_in_the_service_layer = classes()
            .that()
            .areAnnotatedWith(Transactional.class)
            .should()
            .resideInAPackage(Packages.SERVICE)
            .because("transaction boundaries are drawn around use cases in the service layer");

    @ArchTest
    static final ArchRule transactional_methods_are_public = methods()
            .that()
            .areAnnotatedWith(Transactional.class)
            .should()
            .bePublic()
            .because("Spring's transactional proxy only applies to public methods; "
                    + "@Transactional on a non-public method silently does nothing");

    @ArchTest
    static final ArchRule services_are_read_only_transactional_at_class_level = classes()
            .that()
            .areAnnotatedWith(Service.class)
            .should(ArchConditions.beAnnotatedWithReadOnlyTransactional())
            .because("read-only is the safe default; writing methods opt in with a method-level @Transactional");
}
