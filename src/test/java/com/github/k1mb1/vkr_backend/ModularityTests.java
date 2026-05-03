package com.github.k1mb1.vkr_backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    private static final ApplicationModules modules =
            ApplicationModules.of(VkrBackendApplication.class);

    @Test
    void verifiesModuleStructure() {
        modules.verify();
    }

    @Test
    void documentsModules() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
