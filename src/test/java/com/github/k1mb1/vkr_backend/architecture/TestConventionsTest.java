package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/**
 * Conventions for the test code itself. Unlike the other rule classes this one
 * deliberately imports the test sources too (no {@code DoNotIncludeTests}), since
 * the subject — methods annotated with {@code @Test} — only exists there.
 */
@AnalyzeClasses(packages = Packages.ROOT, importOptions = ExcludeGenerated.class)
class TestConventionsTest {

    @ArchTest
    static final ArchRule test_classes_are_named_consistently = methods()
            .that()
            .areAnnotatedWith(Test.class)
            .should()
            .beDeclaredInClassesThat()
            .haveSimpleNameEndingWith("Test")
            .because("any class holding @Test methods is a *Test (a single, uniform suffix)");

    @ArchTest
    static final ArchRule test_classes_are_package_private = classes()
            .that()
            .haveSimpleNameEndingWith("Test")
            .should()
            .bePackagePrivate()
            .because("JUnit 5 discovers package-private test classes; a public modifier is needless noise");

    @ArchTest
    static final ArchRule test_methods_are_not_public = methods()
            .that()
            .areAnnotatedWith(Test.class)
            .should()
            .notBePublic()
            .because("JUnit 5 needs no public modifier on @Test methods; keep them package-private");
}
