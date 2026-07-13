package com.github.k1mb1.vkr_backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composed annotation that carries the one import configuration shared by every
 * architecture rule suite weighing production code: analyse the application
 * packages, skip test sources and skip generated artefacts. Declaring it once
 * here removes the repeated {@code @AnalyzeClasses(...)} boilerplate while letting
 * each {@code *Rules} suite stay self-contained and individually runnable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@AnalyzeClasses(
        packages = Packages.ROOT,
        importOptions = {DoNotIncludeTests.class, ExcludeGenerated.class})
@interface AnalyzeProductionClasses {}
