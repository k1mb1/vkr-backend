import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

/**
 * The project's whole static-analysis stack in one applyable preset:
 * Spotless (palantir format), Checkstyle, PMD, SpotBugs (+ findsecbugs & fb-contrib),
 * Error Prone / NullAway, Forbidden APIs and Modernizer. Apply with
 * `plugins { id("vkr.lint-conventions") }`; rule configs live in the consumer's
 * `config/` directory. Tool versions are pinned in buildSrc so the whole gate moves
 * as one unit.
 */

plugins {
    java
    checkstyle
    pmd
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
    id("com.github.spotbugs")
    id("de.thetaphi.forbiddenapis")
    id("com.github.andygoossens.modernizer")
}

dependencies {
    "errorprone"("com.google.errorprone:error_prone_core:2.50.0")
    "errorprone"("com.uber.nullaway:nullaway:0.13.7")
    "spotbugsPlugins"("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")
    "spotbugsPlugins"("com.mebigfatguy.sb-contrib:sb-contrib:7.6.9")
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat("2.96.0")
        importOrder()
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("*.gradle.kts", "*.md", ".gitignore", "**/*.yaml", "**/*.yml", "**/*.sql")
        targetExclude("**/build/**", "**/.gradle/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

checkstyle {
    toolVersion = "13.8.0"
    maxWarnings = 0
}

pmd {
    toolVersion = "7.26.0"
    ruleSets = emptyList()
    ruleSetFiles = files("config/pmd/ruleset.xml")
    isConsoleOutput = true
}

tasks.named<Pmd>("pmdTest") {
    ruleSetFiles = files("config/pmd/ruleset-test.xml")
}

spotbugs {
    effort = Effort.MAX
    reportLevel = Confidence.HIGH
    excludeFilter = file("config/spotbugs/exclude.xml")
}

tasks.withType<SpotBugsTask>().configureEach {
    reports.create("html") { required = true }
    reports.create("xml") { required = true }
}

// Bans error-prone JDK calls the other tools don't see: charset/locale-implicit
// methods (getBytes(), toLowerCase(), ...), stray System.out/err, non-portable APIs.
forbiddenApis {
    bundledSignatures = setOf("jdk-unsafe", "jdk-non-portable", "jdk-system-out")
    failOnUnresolvableSignatures = false
}

tasks.withType<CheckForbiddenApis>().configureEach {
    enabled = !name.contains("aot", ignoreCase = true)
}

// Tests may use LocalDate.now()/default-charset fixtures; keep the strict
// timezone/locale/charset ban (jdk-unsafe) on production code only.
tasks.named<CheckForbiddenApis>("forbiddenApisTest") {
    bundledSignatures = setOf("jdk-system-out", "jdk-non-portable")
}

// Flags legacy APIs that have a modern JDK equivalent (new Integer(), StringBuffer,
// Guava helpers superseded by java.util, ...).
modernizer {
    failOnViolations = true
    includeTestClasses = true
}

tasks.withType<Checkstyle>().configureEach {
    enabled = !name.contains("aot", ignoreCase = true)
}
tasks.withType<Pmd>().configureEach {
    enabled = !name.contains("aot", ignoreCase = true)
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableWarningsInGeneratedCode = true
        excludedPaths = ".*/build/generated/.*"
        disable("EqualsGetClass")
        option("NullAway:JSpecifyMode", "true")
        option("NullAway:AnnotatedPackages", "com.github.k1mb1.vkr_backend")
        option(
            "NullAway:ExcludedClassAnnotations",
            "jakarta.persistence.Entity,jakarta.persistence.MappedSuperclass",
        )
        // Container-injected fields are guaranteed initialized outside the constructor.
        option("NullAway:ExcludedFieldAnnotations", "jakarta.persistence.PersistenceContext")
        check("NullAway", CheckSeverity.ERROR)

        // Modern-baseline correctness checks promoted from warning to error so a real
        // bug fails the build instead of scrolling past as a warning. All are clean today.
        // Note: OperatorPrecedence stays a warning on purpose — it contradicts
        // Checkstyle's UnnecessaryParentheses (one wants parens where the other forbids them).
        listOf(
            "MissingOverride",
            "ReferenceEquality",
            "FallThrough",
            "MissingCasesInEnumSwitch",
            "FutureReturnValueIgnored",
            "NarrowingCompoundAssignment",
            "BadImport",
            "InconsistentCapitalization",
            "ObjectToString",
            "BoxedPrimitiveEquality",
            "EqualsUnsafeCast",
            "TypeParameterUnusedInFormals",
            "UnnecessaryLambda",
            "NonOverridingEquals",
        )
            .forEach { check(it, CheckSeverity.ERROR) }
    }
}

tasks.named<JavaCompile>("compileTestJava") {
    options.errorprone {
        check("NullAway", CheckSeverity.OFF)
    }
}
