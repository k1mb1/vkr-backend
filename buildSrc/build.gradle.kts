plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Plugin markers so the precompiled `vkr.lint-conventions` script can apply these
    // by id. These versions are the single source of truth for the lint tool stack.
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.8.0")
    implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:5.1.0")
    implementation("com.github.spotbugs:com.github.spotbugs.gradle.plugin:6.5.9")
    implementation("de.thetaphi.forbiddenapis:de.thetaphi.forbiddenapis.gradle.plugin:3.9")
    implementation("com.github.andygoossens.modernizer:com.github.andygoossens.modernizer.gradle.plugin:1.11.0")
}
