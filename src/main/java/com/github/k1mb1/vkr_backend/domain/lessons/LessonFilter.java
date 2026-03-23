package com.github.k1mb1.vkr_backend.domain.lessons;

import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record LessonFilter(String nameLike, LessonType type, UUID subjectId) {
    public Specification<LessonEntity> toSpecification() {
        return Specification.where(nameLikeSpec())
            .and(typeSpec())
            .and(subjectIdSpec());
    }

    private Specification<LessonEntity> nameLikeSpec() {
        return (
            (root, query, cb) ->
                StringUtils.hasText(nameLike)
                    ? cb.like(
                          cb.lower(root.get("name")),
                          "%" + nameLike.toLowerCase() + "%"
                      )
                    : null
        );
    }

    private Specification<LessonEntity> typeSpec() {
        return (
            (root, query, cb) ->
                type != null ? cb.equal(root.get("type"), type) : null
        );
    }

    private Specification<LessonEntity> subjectIdSpec() {
        return (
            (root, query, cb) ->
                subjectId != null
                    ? cb.equal(root.get("subjectId"), subjectId)
                    : null
        );
    }
}
