package com.github.k1mb1.vkr_backend.domain.teachers;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record TeacherFilter(String nameLike) {
    public Specification<TeacherEntity> toSpecification() {
        return nameLikeSpec();
    }

    private Specification<TeacherEntity> nameLikeSpec() {
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
}
