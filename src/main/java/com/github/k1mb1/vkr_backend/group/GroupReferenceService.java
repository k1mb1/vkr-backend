package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public interface GroupReferenceService {

    Group getGroupReferenceById(UUID id);

    Subgroup getSubgroupReferenceById(UUID id);

    /**
     * Разрешает аудиторию (группа + опц. подгруппа) в ссылки на сущности с проверкой
     * принадлежности подгруппы группе. {@code groupId == null} → пустая аудитория
     * (вызывающий трактует как allGroups). Бросает {@link IllegalArgumentException},
     * если подгруппа не принадлежит указанной группе.
     */
    AudienceRef resolveAudience(@Nullable UUID groupId, @Nullable UUID allowedSubgroupId);
}
