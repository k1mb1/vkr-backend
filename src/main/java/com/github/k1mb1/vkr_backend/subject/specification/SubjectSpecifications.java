package com.github.k1mb1.vkr_backend.subject.specification;

import com.github.k1mb1.vkr_backend.common.persistence.Specs;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Спецификация поиска предметов. Видимость («свои предметы») приходит уже
 * разрешённым набором id из порта {@code SubjectVisibilityPort} — subject не
 * знает о таблице прав teacher.
 */
public record SubjectSpecifications(String name, @Nullable Set<UUID> visibleSubjectIds) {

    public Specification<SubjectEntity> toSpecification() {
        return Specs.<SubjectEntity>containsIgnoreCase("name", name).and(visibleSpec());
    }

    private Specification<SubjectEntity> visibleSpec() {
        return (root, query, cb) ->
                visibleSubjectIds == null ? null : root.get("id").in(visibleSubjectIds);
    }
}
