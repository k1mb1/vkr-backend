package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

/**
 * Long-term maintainability guardrails — the boundaries that keep the backend
 * easy to reason about and cheap to change as it grows. Unlike the layering or
 * naming suites these are not about <em>where</em> code lives but about habits
 * that quietly rot a codebase: shared mutable state, misused {@code Optional},
 * configuration read in scattered places, and persistence reached behind the
 * repositories' back. Each rule states its boundary in {@code because(...)}.
 */
@AnalyzeProductionClasses
class MaintainabilityRules {

    /** No mutable global state: a static field that is not final is shared, racy and hard to trace. */
    @ArchTest
    static final ArchRule static_fields_are_final = fields().that()
            .areStatic()
            .should()
            .beFinal()
            .because("mutable static state is shared across threads and requests and is a classic source of "
                    + "hard-to-reproduce bugs; keep static fields final (constants, loggers)");

    /** {@code Optional} is a return type, not a field. */
    @ArchTest
    static final ArchRule optional_is_not_a_field = noFields()
            .should()
            .haveRawType(Optional.class)
            .because("Optional models an optional return value; as a field it only adds wrapping — "
                    + "use a plain @Nullable field or an empty collection");

    /** {@code Optional} is a return type, not a parameter. */
    @ArchTest
    static final ArchRule optional_is_not_a_parameter = methods()
            .should(ArchConditions.notUseOptionalAsParameter())
            .because(
                    "an Optional parameter forces callers to wrap arguments; overload or accept the value/null instead");

    /** Configuration is bound in one place: {@code @Value} reads live only in the config layer. */
    @ArchTest
    static final ArchRule property_injection_is_confined_to_config = fields().that()
            .areAnnotatedWith(Value.class)
            .should()
            .beDeclaredInClassesThat()
            .resideInAPackage(Packages.CONFIG)
            .allowEmptyShould(true)
            .because("@Value reads scattered across beans are hard to discover and test; "
                    + "bind configuration in ..config.. and inject typed values");

    /** Database access goes through Spring Data repositories, never a raw EntityManager. */
    @ArchTest
    static final ArchRule persistence_access_is_confined_to_repositories = noClasses()
            .that()
            .resideOutsideOfPackage(Packages.REPOSITORY)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("jakarta.persistence.EntityManager")
            .because("persistence is reached only through *Repository interfaces; a raw EntityManager "
                    + "elsewhere spreads query logic across layers");
}
