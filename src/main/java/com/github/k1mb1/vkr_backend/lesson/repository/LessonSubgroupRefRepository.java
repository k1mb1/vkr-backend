package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.repository.Repository;

/** Ссылки модуля lesson на подгруппы (group::domain) с проверкой принадлежности группе. */
public interface LessonSubgroupRefRepository extends Repository<SubgroupEntity, UUID> {

    SubgroupEntity getReferenceById(UUID id);

    /**
     * Reference на подгруппу с проверкой, что она принадлежит указанной группе
     * (аудитория «группа + подгруппа» согласована). {@code null} на входе — нет
     * ограничения по подгруппе.
     */
    default @Nullable SubgroupEntity resolveAllowedSubgroup(@Nullable UUID subgroupId, UUID groupId) {
        if (subgroupId == null) {
            return null;
        }
        var subgroup = getReferenceById(subgroupId);
        if (!Objects.equals(subgroup.getGroup().getId(), groupId)) {
            throw new IllegalArgumentException("Subgroup does not belong to the specified group");
        }
        return subgroup;
    }
}
