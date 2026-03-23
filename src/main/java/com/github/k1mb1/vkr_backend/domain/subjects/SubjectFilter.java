package com.github.k1mb1.vkr_backend.domain.subjects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record SubjectFilter(String nameLike) {
    public Specification<SubjectEntity> toSpecification() {
        return nameLikeSpec();
    }

    private Specification<SubjectEntity> nameLikeSpec() {
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
