package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the in-module layering convention. Layers are matched by package
 * segment, so every module's sub-packages participate:
 *
 * <pre>
 *   &lt;module&gt;/controller     -&gt; Controller     (REST controllers)
 *   &lt;module&gt;/service        -&gt; Service        (use cases / business logic)
 *   &lt;module&gt;/mapper         -&gt; Mapper         (MapStruct entity &lt;-&gt; DTO mappers)
 *   &lt;module&gt;/specification  -&gt; Specification  (JPA Specification builders)
 *   &lt;module&gt;/domain         -&gt; Domain         (entities, domain logic)
 *   &lt;module&gt;/repository     -&gt; Repository     (Spring Data repositories)
 *   &lt;module&gt;/config         -&gt; Config         (Spring configuration)
 * </pre>
 *
 * Allowed access flows top-down toward the domain; the domain depends on nothing.
 * Module-to-module boundaries are enforced separately by {@link ModularityTest}
 * and {@link LayerBoundaryRules}.
 */
@AnalyzeProductionClasses
class LayeredArchitectureRules {

    @ArchTest
    static final ArchRule layered_architecture_is_respected = layeredArchitecture()
            // Only weigh dependencies between our own classes; ignore framework
            // packages (jakarta.*, spring, java.*).
            .consideringOnlyDependenciesInAnyPackage(Packages.ROOT + "..")
            // Layers, matched by package segment across all modules.
            .layer("Controller")
            .definedBy(Packages.CONTROLLER)
            .layer("Service")
            .definedBy(Packages.SERVICE)
            .layer("Mapper")
            .definedBy(Packages.MAPPER)
            .layer("Specification")
            .definedBy(Packages.SPECIFICATION)
            .layer("Domain")
            .definedBy(Packages.DOMAIN)
            .layer("Repository")
            .definedBy(Packages.REPOSITORY)
            .layer("Config")
            .definedBy(Packages.CONFIG)

            // Controller is the entry point: nothing may depend on it.
            .whereLayer("Controller")
            .mayNotBeAccessedByAnyLayer()

            // Service (use cases) is reachable from Controller; mappers and
            // specifications also touch the service-layer DTOs they consume/produce
            // (..service.dto.. — *Response for mappers, *Filter for specifications).
            .whereLayer("Service")
            .mayOnlyBeAccessedByLayers("Controller", "Mapper", "Specification")

            // Mapper turns entities into DTOs; only the service layer drives it.
            .whereLayer("Mapper")
            .mayOnlyBeAccessedByLayers("Service")

            // Specification builds JPA queries from a *Filter; only the service drives it.
            .whereLayer("Specification")
            .mayOnlyBeAccessedByLayers("Service")

            // Repository is an outbound adapter, driven only by the Service layer
            // (controllers must go through services, never hit repositories directly).
            .whereLayer("Repository")
            .mayOnlyBeAccessedByLayers("Service")

            // Domain is the core: everyone may use it, it depends on no layer.
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Controller", "Service", "Mapper", "Specification", "Repository")
            .whereLayer("Domain")
            .mayNotAccessAnyLayer()

            // Config wires things up; it must not be depended on as a layer.
            .whereLayer("Config")
            .mayNotBeAccessedByAnyLayer()

            // Layers not yet populated are fine.
            .withOptionalLayers(true)
            .because("dependencies flow controller -> service -> repository -> domain; "
                    + "the domain is the core and depends on no layer (see ARCHITECTURE.md)");
}
