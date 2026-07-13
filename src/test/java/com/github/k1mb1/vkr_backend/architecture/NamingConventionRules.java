package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Naming and stereotype-placement conventions: a type's package, its name suffix
 * and its Spring stereotype must agree, so a class can be located and understood
 * from any one of the three. Complements {@link LayeredArchitectureRules}
 * (dependency direction) and {@link LayerBoundaryRules} (leak prevention).
 */
@AnalyzeProductionClasses
class NamingConventionRules {

    @ArchTest
    static final ArchRule controllers_are_named_and_annotated = classes()
            .that()
            .resideInAPackage(Packages.CONTROLLER)
            .should()
            .haveSimpleNameEndingWith("Controller")
            .andShould()
            .beAnnotatedWith(RestController.class)
            .because("a *Controller in ..controller.. is the web entry point of a module");

    @ArchTest
    static final ArchRule services_are_named_and_annotated = classes()
            .that()
            .resideInAPackage(Packages.SERVICE)
            .and()
            .areNotRecords()
            .and()
            .resideOutsideOfPackage(Packages.DTO)
            .should()
            .haveSimpleNameEndingWith("Service")
            .andShould()
            .beAnnotatedWith(Service.class)
            .because("application logic lives in *Service beans inside ..service..");

    @ArchTest
    static final ArchRule repositories_are_interfaces_named_consistently = classes()
            .that()
            .resideInAPackage(Packages.REPOSITORY)
            .and()
            // nested projection views are namespaced by their owning repository
            .areTopLevelClasses()
            .should()
            .beInterfaces()
            .andShould()
            .haveSimpleNameEndingWith("Repository")
            .andShould()
            .beAssignableTo(Repository.class)
            .because("persistence is reached only through Spring Data *Repository interfaces");

    @ArchTest
    static final ArchRule config_classes_are_named_and_annotated = classes()
            .that()
            .resideInAPackage(Packages.CONFIG)
            .should()
            .haveSimpleNameEndingWith("Config")
            .andShould()
            .beAnnotatedWith(Configuration.class)
            .because("Spring wiring lives in *Config @Configuration classes inside ..config..");

    @ArchTest
    static final ArchRule dtos_are_records = classes()
            .that()
            .resideInAPackage(Packages.DTO)
            .should()
            .beRecords()
            .because("DTOs are immutable transport types");

    @ArchTest
    static final ArchRule dtos_live_in_a_typed_subpackage = classes()
            .that()
            .resideInAPackage(Packages.DTO)
            .and()
            .doNotHaveSimpleName("package-info")
            .should()
            .resideInAnyPackage(Packages.DTO_REQUEST, Packages.DTO_RESPONSE, Packages.DTO_FILTER)
            .because("DTOs are always split by direction: request/, response/ or filter/ — "
                    + "never loose in service.dto (keeps a large module's DTOs navigable)");

    @ArchTest
    static final ArchRule request_dtos_are_named_request = classes()
            .that()
            .resideInAPackage(Packages.DTO_REQUEST)
            .and()
            .areTopLevelClasses()
            .should()
            .haveSimpleNameEndingWith("Request")
            .because("an inbound DTO in service.dto.request is a *Request");

    @ArchTest
    static final ArchRule response_dtos_are_named_response = classes()
            .that()
            .resideInAPackage(Packages.DTO_RESPONSE)
            .and()
            .areTopLevelClasses()
            .should()
            .haveSimpleNameEndingWith("Response")
            .because("an outbound DTO in service.dto.response is a *Response");

    @ArchTest
    static final ArchRule filter_dtos_are_named_filter = classes()
            .that()
            .resideInAPackage(Packages.DTO_FILTER)
            .and()
            .areTopLevelClasses()
            .should()
            .haveSimpleNameEndingWith("Filter")
            .allowEmptyShould(true)
            .because("a query-criteria DTO in service.dto.filter is a *Filter");

    // --- Stereotype / annotation placement ----------------------------------

    @ArchTest
    static final ArchRule stereotypes_reside_in_their_layer = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage(Packages.CONTROLLER)
            .andShould(ArchConditions.haveExactlyOneConstructor())
            .because("a @RestController belongs in ..controller.. and uses constructor injection");

    @ArchTest
    static final ArchRule service_beans_reside_in_their_layer = classes()
            .that()
            .areAnnotatedWith(Service.class)
            .should()
            .resideInAPackage(Packages.SERVICE)
            .andShould(ArchConditions.haveExactlyOneConstructor())
            .because("a @Service belongs in ..service.. and uses constructor injection");

    @ArchTest
    static final ArchRule repository_interfaces_reside_in_their_layer = classes()
            .that()
            .areAssignableTo(Repository.class)
            .and()
            .areInterfaces()
            .should()
            .resideInAPackage(Packages.REPOSITORY)
            .because("Spring Data repositories belong in ..repository..");

    @ArchTest
    static final ArchRule web_mappings_live_only_in_controllers_methods = methods()
            .that()
            .areMetaAnnotatedWith(RequestMapping.class)
            .should()
            .beDeclaredInClassesThat()
            .resideInAPackage(Packages.CONTROLLER)
            .because("request mappings are a web concern and belong on controllers");

    @ArchTest
    static final ArchRule web_mappings_live_only_in_controllers_classes = classes()
            .that()
            .areAnnotatedWith(RequestMapping.class)
            .should()
            .resideInAPackage(Packages.CONTROLLER)
            .because("request mappings are a web concern and belong on controllers");

    @ArchTest
    static final ArchRule controllers_declare_class_level_request_mapping = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .beAnnotatedWith(RequestMapping.class)
            .because("each controller pins its base path with a class-level @RequestMapping");
}
