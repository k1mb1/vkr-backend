/**
 * Справочник преподавателей. Идентификатор — {@code sub} из IdP (Keycloak), поэтому
 * сущность держит внешний id. Низ графа: от teacher зависит subject (права
 * преподавателей на предметы), сам он зависит только от common.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"common"})
package com.github.k1mb1.vkr_backend.teacher;
