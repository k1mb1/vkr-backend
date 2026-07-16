plugins {
    java
    id("vkr.lint-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
    alias(libs.plugins.hibernate)
    alias(libs.plugins.graalvm.native)
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
    testImplementation(libs.archunit.junit5)
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Nullness annotations (NullAway is wired by the vkr.lint-conventions preset).
    implementation(libs.jspecify)
    compileOnly(libs.spotbugs.annotations)
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

tasks.withType<JavaCompile>().configureEach {
    // Сохраняем имена параметров в class-файлах — их читает Spring Security при
    // резолве `#filter`/`#request`/... внутри @PreAuthorize. Spring Boot 3.2+
    // включает этот флаг сам, но фиксируем явно: в native-image без него SpEL
    // падает с «Failed to evaluate expression» ещё до входа в метод.
    options.compilerArgs.add("-parameters")
}
