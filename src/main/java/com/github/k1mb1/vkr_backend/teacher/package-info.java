/**
 * Справочник преподавателей и выданные им права (право — связь «преподаватель ×
 * предмет» с ограничением аудитории через scope'ы). Использует subject (предмет
 * права), group (аудитория scope) и реализует порты subject
 * ({@code OwnerPermissionGranter}, {@code SubjectVisibilityPort}) и авторизации
 * ({@code auth.api.PermissionAuthPort}).
 */
package com.github.k1mb1.vkr_backend.teacher;
