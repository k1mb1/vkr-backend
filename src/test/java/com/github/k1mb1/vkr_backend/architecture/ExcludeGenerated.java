package com.github.k1mb1.vkr_backend.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

/**
 * {@link ImportOption} that drops compiler/framework-generated artefacts so the
 * rules only ever weigh hand-written code:
 *
 * <ul>
 *   <li>Spring AOT bean definitions, e.g. {@code Foo__BeanDefinitions}.</li>
 *   <li>Spring CGLIB proxies, e.g. {@code FooService$$SpringCGLIB$$0}.</li>
 *   <li>MapStruct mapper implementations, e.g. {@code BookMapperImpl}.</li>
 * </ul>
 *
 * Declared as a named class (rather than a lambda) so it can be referenced from
 * {@code @AnalyzeClasses(importOptions = ...)}, which requires a public type with
 * a no-arg constructor.
 */
public final class ExcludeGenerated implements ImportOption {

    @Override
    public boolean includes(Location location) {
        return !location.contains("__") && !location.contains("$$") && !location.contains("MapperImpl");
    }
}
