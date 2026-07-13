import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    checkstyle
    pmd
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.hibernate)
    alias(libs.plugins.graalvm.native)
    alias(libs.plugins.spotless)
    alias(libs.plugins.errorprone)
    alias(libs.plugins.spotbugs)
}

group = "com.github.k1mb1"
version = "0.0.1-SNAPSHOT"
description = "vkr-backend"

// The Java language level is normally 25 (see CI). It can be overridden for local
// builds on a machine without a JDK 25 toolchain, e.g. `-PjavaToolchainVersion=21`.
val javaToolchainVersion = providers.gradleProperty("javaToolchainVersion")
    .map { it.toInt() }
    .getOrElse(25)

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaToolchainVersion)
    }
}

repositories {
    mavenCentral()
}

// Reproducible builds: every resolvable configuration is locked. Regenerate the
// lockfile after any dependency change with `./gradlew resolveAndLockAll --write-locks`.
dependencyLocking {
    lockAllConfigurations()
}

tasks.register("resolveAndLockAll") {
    notCompatibleWithConfigurationCache("Resolves configurations at execution time")
    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) {
            "Run with --write-locks, e.g. ./gradlew resolveAndLockAll --write-locks"
        }
    }
    doLast {
        configurations.filter { it.isCanBeResolved }.forEach { it.resolve() }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    implementation(libs.springdoc.openapi)
    implementation(libs.mapstruct)

    implementation(libs.spring.modulith.starter.core)

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok.mapstruct.binding)

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation(libs.spring.modulith.starter.test)
    testImplementation(libs.archunit.junit5)
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    errorprone(libs.errorprone.core)
    errorprone(libs.nullaway)
    implementation(libs.jspecify)
    compileOnly(libs.spotbugs.annotations)

    spotbugsPlugins(libs.findsecbugs.plugin)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${libs.versions.springModulith.get()}")
    }
}

// Static Hibernate bytecode enhancement at build time.
// Required for GraalVM native image: with BytecodeProvider 'none' Hibernate cannot
// generate HibernateProxy instances at runtime, so lazy @OneToOne/@ManyToOne
// associations are wired through enhancement instead of runtime proxies.
hibernate {
    enhancement {
    }
}

// Lombok is compileOnly and therefore absent from the runtime classpath used by
// Spring AOT processing, so no explicit AOT exclusion is required (unlike Maven).
graalvmNative {
    binaries {
        named("main") {
            imageName = "vkr-backend"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat("2.94.0")
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
    toolVersion = "13.6.0"
    maxWarnings = 0
}

pmd {
    toolVersion = "7.25.0"
    ruleSets = emptyList()
    ruleSetFiles = files("config/pmd/ruleset.xml")
    isConsoleOutput = true
}

tasks.named<Pmd>("pmdTest") {
    ruleSetFiles = files("config/pmd/ruleset-test.xml")
}

spotbugs {
    effort = com.github.spotbugs.snom.Effort.MAX
    reportLevel = com.github.spotbugs.snom.Confidence.HIGH
    excludeFilter = file("config/spotbugs/exclude.xml")
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports.create("html") { required = true }
    reports.create("xml") { required = true }
}

tasks.withType<Checkstyle>().configureEach {
    enabled = !name.contains("aot", ignoreCase = true)
}
tasks.withType<Pmd>().configureEach {
    enabled = !name.contains("aot", ignoreCase = true)
}

tasks.withType<JavaCompile>().configureEach {
    // Сохраняем имена параметров в class-файлах — их читает Spring Security при
    // резолве `#filter`/`#request`/... внутри @PreAuthorize. Spring Boot 3.2+
    // включает этот флаг сам, но фиксируем явно: в native-image без него SpEL
    // падает с «Failed to evaluate expression» ещё до входа в метод.
    options.compilerArgs.add("-parameters")
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
    }
}

tasks.named<JavaCompile>("compileTestJava") {
    options.errorprone {
        check("NullAway", CheckSeverity.OFF)
    }
}
