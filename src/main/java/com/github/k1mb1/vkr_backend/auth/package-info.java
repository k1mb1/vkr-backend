/**
 * Аутентификация/авторизация: identity из JWT ({@code SecurityService}),
 * кэшируемый снапшот тонких прав ({@code PermissionResolver}) и точка решений
 * {@code @authz} для SpEL в {@code @PreAuthorize}. Данные о правах и принадлежности
 * ресурсов поступают через порты {@code auth.api}, реализуемые сервисами
 * subject/lesson/journal.
 */
package com.github.k1mb1.vkr_backend.auth;
