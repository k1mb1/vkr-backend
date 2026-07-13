package com.github.k1mb1.vkr_backend.auth.api;

import java.util.UUID;

/**
 * Одно выданное преподавателю право в снапшоте авторизации: id права, предмет,
 * к которому оно относится, и признак полного доступа ({@code allPermissions}).
 */
public record PermissionGrantResponse(UUID permissionId, UUID subjectId, boolean allPermissions) {}
