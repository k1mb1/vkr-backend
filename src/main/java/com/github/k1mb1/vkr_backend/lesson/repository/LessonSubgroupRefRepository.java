package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Ссылки модуля lesson на подгруппы (group::domain) с проверкой принадлежности группе. */
public interface LessonSubgroupRefRepository extends Repository<SubgroupEntity, UUID> {

    SubgroupEntity getReferenceById(UUID id);

    /**
     * Существует ли подгруппа с данным id у данной группы. Проверка принадлежности одним
     * count-запросом, без загрузки сущности и её ленивой {@code group}: прежний
     * {@code subgroup.getGroup().getId()} в циклах мутаций scope'ов плодил N+1.
     */
    @Query("SELECT COUNT(s) > 0 FROM SubgroupEntity s WHERE s.id = :subgroupId AND s.group.id = :groupId")
    boolean existsByIdAndGroupId(@Param("subgroupId") UUID subgroupId, @Param("groupId") UUID groupId);

    /**
     * Reference на подгруппу с проверкой, что она принадлежит указанной группе
     * (аудитория «группа + подгруппа» согласована). {@code null} на входе — нет
     * ограничения по подгруппе.
     */
    default @Nullable SubgroupEntity resolveAllowedSubgroup(@Nullable UUID subgroupId, UUID groupId) {
        if (subgroupId == null) {
            return null;
        }
        if (!existsByIdAndGroupId(subgroupId, groupId)) {
            throw new IllegalArgumentException("Subgroup does not belong to the specified group");
        }
        return getReferenceById(subgroupId);
    }
}
