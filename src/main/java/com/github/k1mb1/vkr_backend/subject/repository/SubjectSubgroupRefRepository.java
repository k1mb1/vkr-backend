package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.repository.Repository;

/** Ссылки модуля subject на подгруппы (group::domain) с проверкой принадлежности группе. */
public interface SubjectSubgroupRefRepository extends Repository<SubgroupEntity, UUID> {

    SubgroupEntity getReferenceById(UUID id);

    /** Reference на подгруппу с проверкой, что она принадлежит указанной группе. */
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
