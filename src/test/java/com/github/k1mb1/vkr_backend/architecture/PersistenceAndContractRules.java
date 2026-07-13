package com.github.k1mb1.vkr_backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Persistence-mapping invariants and the public service contract. Entities share
 * one identity/auditing base and an explicit table, and never leak out of the
 * service API.
 */
@AnalyzeProductionClasses
class PersistenceAndContractRules {

    @ArchTest
    static final ArchRule entities_live_in_domain_and_are_named_consistently = classes()
            .that()
            .areAnnotatedWith(Entity.class)
            .should()
            .resideInAPackage(Packages.DOMAIN)
            .andShould()
            .haveSimpleNameEndingWith("Entity")
            .because("a persisted *Entity is domain state and lives in ..domain..");

    @ArchTest
    static final ArchRule entities_extend_base_entity_and_declare_table = classes()
            .that()
            .areAnnotatedWith(Entity.class)
            .should()
            .beAssignableTo(BaseEntity.class)
            .andShould()
            .beAnnotatedWith(Table.class)
            .because("every entity inherits identity + auditing from BaseEntity and names its table explicitly");

    @ArchTest
    static final ArchRule entities_are_not_final = classes()
            .that()
            .areAnnotatedWith(Entity.class)
            .should()
            .notHaveModifier(JavaModifier.FINAL)
            .because("Hibernate subclasses entities to build lazy proxies; a final entity (e.g. a record) breaks that");

    @ArchTest
    static final ArchRule services_expose_only_dtos_and_never_take_entities = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(Packages.SERVICE)
            .and()
            .areDeclaredInClassesThat()
            .resideOutsideOfPackage(Packages.DTO)
            .and()
            .arePublic()
            .should(ArchConditions.exposeOnlyDtosOrVoid())
            .because("services never leak entities: they return *Response DTOs (or void) and take requests/ids");
}
