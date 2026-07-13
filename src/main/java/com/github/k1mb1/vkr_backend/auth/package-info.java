/**
 * Модуль аутентификации/авторизации: identity из JWT ({@code SecurityService}),
 * кэшируемый снапшот тонких прав ({@code PermissionResolver}) и точка решений
 * {@code @authz} для SpEL в {@code @PreAuthorize}. Данные о правах и принадлежности
 * ресурсов поступают через SPI-порты {@code auth.api}, реализуемые модулями
 * subject/lesson/attendance, — поэтому auth остаётся листом графа и не может
 * образовать цикл с бизнес-модулями.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"common"})
package com.github.k1mb1.vkr_backend.auth;
