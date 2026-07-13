package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Cross-cutting coding hygiene that no formatter or compiler enforces: bean field
 * visibility, exception placement, the date API, injection style and logging.
 * Wraps ArchUnit's library {@code GeneralCodingRules} alongside the project's own.
 */
@AnalyzeProductionClasses
class CodingHygieneRules {

    @ArchTest
    static final ArchRule bean_fields_are_private_and_final = fields().that()
            .areDeclaredInClassesThat()
            .resideInAnyPackage(Packages.CONTROLLER, Packages.SERVICE)
            .and()
            .areNotStatic()
            .should()
            .bePrivate()
            .andShould()
            .beFinal()
            .because("collaborators are injected once via the constructor and never reassigned");

    // --- Exceptions ---------------------------------------------------------

    @ArchTest
    static final ArchRule custom_exceptions_are_named_and_placed = classes()
            .that()
            .areAssignableTo(Throwable.class)
            .should()
            .haveSimpleNameEndingWith("Exception")
            .andShould()
            .resideInAPackage(Packages.EXCEPTION)
            .because("custom failures are discoverable *Exception types under ..exception..");

    // HTTP-status mapping is centralised in common.web.GlobalExceptionHandler (see
    // WebContractRules), so we no longer force a @ResponseStatus on every exception —
    // an exception may instead be mapped by the advice.

    @ArchTest
    static final ArchRule custom_exceptions_are_unchecked = classes()
            .that()
            .areAssignableTo(Throwable.class)
            .should()
            .beAssignableTo(RuntimeException.class)
            .allowEmptyShould(true)
            .because("we use unchecked exceptions only; checked exceptions force try/catch boilerplate up the stack");

    @ArchTest
    static final ArchRule no_catching_generic_exceptions = classes()
            .that()
            .resideOutsideOfPackage(Packages.COMMON)
            .should(ArchConditions.notCatchGenericExceptions())
            .because("catching Exception/Throwable swallows the failure you did not anticipate; catch the "
                    + "specific type (the global handler in common.web is the one place that maps the catch-all)");

    // --- APIs we standardise on ---------------------------------------------

    @ArchTest
    static final ArchRule uses_java_time_not_legacy_date_api = noClasses()
            .should()
            .dependOnClassesThat(equivalentTo(java.util.Date.class)
                    .or(equivalentTo(java.util.Calendar.class))
                    .or(equivalentTo(java.text.SimpleDateFormat.class)))
            .as("classes should use java.time instead of legacy java.util/text date types")
            .because("java.time is immutable and unambiguous");

    @ArchTest
    static final ArchRule no_method_injection = noMethods()
            .should()
            .beAnnotatedWith(Autowired.class)
            .because("collaborators are wired through constructors, not setters");

    @ArchTest
    static final ArchRule loggers_follow_the_slf4j_convention = fields().that()
            .haveRawType("org.slf4j.Logger")
            .should()
            .bePrivate()
            .andShould()
            .beStatic()
            .andShould()
            .beFinal()
            .andShould()
            .haveName("log")
            .allowEmptyShould(true)
            .because("we log via Lombok @Slf4j, which generates a private static final Logger named \"log\"");

    // --- ArchUnit library hygiene rules -------------------------------------

    @ArchTest
    static final ArchRule no_access_to_standard_streams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static final ArchRule no_generic_exceptions_thrown = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    static final ArchRule no_java_util_logging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    static final ArchRule no_field_injection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
}
