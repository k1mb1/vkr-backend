package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.mapstruct.Mapper;

/**
 * Conventions for the MapStruct mapping layer. Mappers are the single place where
 * a persisted {@code *Entity} is translated into a transport {@code *Response}, so
 * they are the only non-service types allowed to touch both the domain and the
 * DTOs at once. These rules keep that layer narrow: declarative {@code @Mapper}
 * interfaces, generated as Spring beans, that never reach past the domain into
 * persistence or the web. The generated {@code *MapperImpl} classes are filtered
 * out by {@link ExcludeGenerated}, so only the hand-written interfaces are weighed.
 */
@AnalyzeProductionClasses
class MapperRules {

    @ArchTest
    static final ArchRule mappers_are_named_and_annotated = classes()
            .that()
            .resideInAPackage(Packages.MAPPER)
            .and()
            .doNotHaveSimpleName("package-info")
            .should()
            .beInterfaces()
            .andShould()
            .haveSimpleNameEndingWith("Mapper")
            .andShould()
            .beAnnotatedWith(Mapper.class)
            .allowEmptyShould(true)
            .because("mapping logic lives in declarative @Mapper interfaces named *Mapper inside ..mapper..");

    @ArchTest
    static final ArchRule mapper_types_reside_in_their_layer = classes()
            .that()
            .areAnnotatedWith(Mapper.class)
            .should()
            .resideInAPackage(Packages.MAPPER)
            .allowEmptyShould(true)
            .because("a @Mapper is the mapping layer and belongs in ..mapper..");

    @ArchTest
    static final ArchRule mappers_are_spring_beans = classes()
            .that()
            .areAnnotatedWith(Mapper.class)
            .should(ArchConditions.useSpringComponentModel())
            .allowEmptyShould(true)
            .because("mappers are injected into services by constructor, so they must be Spring beans");

    @ArchTest
    static final ArchRule mappers_do_not_reach_into_persistence_or_web = noClasses()
            .that()
            .resideInAPackage(Packages.MAPPER)
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(Packages.REPOSITORY, Packages.CONTROLLER, Packages.SPRING_HTTP, Packages.SERVLET)
            .because("mappers translate domain entities to DTOs only: no repository, controller or web types");
}
