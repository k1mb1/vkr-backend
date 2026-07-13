package com.github.k1mb1.vkr_backend.architecture;

import com.github.k1mb1.vkr_backend.VkrBackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the application's module structure (one module per top-level package
 * under {@code com.github.k1mb1.vkr_backend}) and generates the module documentation.
 * This is pure static analysis built on ArchUnit, so it needs no Spring context.
 */
class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(VkrBackendApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void writesDocumentationSnippets() {
        new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
    }
}
