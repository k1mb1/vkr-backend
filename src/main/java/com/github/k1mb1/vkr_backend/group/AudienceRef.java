package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import org.jspecify.annotations.Nullable;

/**
 * Разрешённая аудитория: ссылка на группу и (опционально) подгруппу.
 * {@code group == null} означает «все группы» (allGroups на стороне вызывающего).
 */
public record AudienceRef(
        @Nullable GroupEntity group, @Nullable SubgroupEntity allowedSubgroup) {}
