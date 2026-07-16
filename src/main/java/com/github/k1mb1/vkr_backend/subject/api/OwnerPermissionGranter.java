package com.github.k1mb1.vkr_backend.subject.api;

import java.util.UUID;

/**
 * Порт, объявленный subject и реализуемый teacher (инверсия зависимости): при
 * создании предмета его владельцу выдаётся полное право. subject не знает о
 * сущностях teacher/permission — только вызывает этот порт скалярными id.
 */
public interface OwnerPermissionGranter {

    /** Выдать преподавателю полное право на предмет (авто-грант владельцу при создании). */
    void grantAllPermissions(UUID teacherId, UUID subjectId);
}
