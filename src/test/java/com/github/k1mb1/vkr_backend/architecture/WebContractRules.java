package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.github.k1mb1.vkr_backend.common.error.ErrorDto;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The HTTP contract every controller endpoint must honour: a uniform
 * {@link ResponseEntity} return type (so the status is set explicitly), bean
 * validation on every request body, and a single cross-cutting error-handling
 * surface.
 */
@AnalyzeProductionClasses
class WebContractRules {

    @ArchTest
    static final ArchRule endpoints_return_response_entity = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(Packages.CONTROLLER)
            .and()
            .arePublic()
            .should()
            .haveRawReturnType(ResponseEntity.class)
            .because("endpoints set the HTTP status explicitly by returning a ResponseEntity");

    @ArchTest
    static final ArchRule request_bodies_are_validated = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(Packages.CONTROLLER)
            .and()
            .arePublic()
            .should(ArchConditions.validateEveryRequestBody())
            .because("a @RequestBody without @Valid silently skips bean validation");

    @ArchTest
    static final ArchRule request_bodies_are_request_dtos = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(Packages.CONTROLLER)
            .and()
            .arePublic()
            .should(ArchConditions.bindRequestBodyToRequestDto())
            .because("a @RequestBody is the inbound *Request DTO the service consumes, "
                    + "not an entity or an ad-hoc type");

    @ArchTest
    static final ArchRule path_and_query_parameters_are_simple_values = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(Packages.CONTROLLER)
            .and()
            .arePublic()
            .should(ArchConditions.bindPathAndQueryParamsToSimpleValues())
            .because("@PathVariable/@RequestParam carry scalar values (ids, flags); structured input "
                    + "arrives as a @RequestBody *Request or a bound *Filter, never a DTO/entity parameter");

    @ArchTest
    static final ArchRule endpoints_have_descriptive_names = methods()
            .that()
            .areMetaAnnotatedWith(RequestMapping.class)
            .should(ArchConditions.nameEndpointByHttpMethodAndResource())
            .because("a handler method name becomes the OpenAPI operationId; derive the verb from the HTTP "
                    + "method and add the resource (getBook, createBook), or <action><Resource> for sub-resource "
                    + "actions (returnLoan) — never a bare verb that springdoc must disambiguate");

    @ArchTest
    static final ArchRule bound_filter_and_pageable_are_documented = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(Packages.CONTROLLER)
            .and()
            .arePublic()
            .should(ArchConditions.documentBoundFilterAndPageable())
            .because("a bound *Filter / Pageable is expanded into OpenAPI query parameters: "
                    + "a *Filter is @ParameterObject + @ModelAttribute, a Pageable is @ParameterObject");

    // --- Centralised error handling -----------------------------------------

    @ArchTest
    static final ArchRule controller_advice_is_a_named_handler_in_common = classes()
            .that()
            .areMetaAnnotatedWith(ControllerAdvice.class)
            .should()
            .resideInAPackage(Packages.COMMON)
            .andShould()
            .haveSimpleNameEndingWith("ExceptionHandler")
            .allowEmptyShould(true)
            .because("error handling is cross-cutting: a single *ExceptionHandler @ControllerAdvice lives in "
                    + "common.web, so every endpoint shares one error contract");

    @ArchTest
    static final ArchRule exception_handlers_return_the_error_dto = methods()
            .that()
            .areAnnotatedWith(ExceptionHandler.class)
            .should()
            .haveRawReturnType(ErrorDto.class)
            .allowEmptyShould(true)
            .because("every handled exception leaves as the single uniform ErrorDto body "
                    + "(the HTTP status is set with @ResponseStatus)");
}
