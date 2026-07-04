package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import org.jspecify.annotations.Nullable;

/**
 * Разрешённая аудитория: ссылка на группу и (опционально) подгруппу.
 * {@code group == null} означает «все группы» (allGroups на стороне вызывающего).
 */
public record AudienceRef(@Nullable Group group, @Nullable Subgroup allowedSubgroup) {}
