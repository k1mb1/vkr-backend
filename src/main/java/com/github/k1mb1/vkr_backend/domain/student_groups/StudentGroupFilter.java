package com.github.k1mb1.vkr_backend.domain.student_groups;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record StudentGroupFilter(String nameLike) {

    public Specification<StudentGroupEntity> toSpecification() {
        return nameLikeSpec();
    }

    private Specification<StudentGroupEntity> nameLikeSpec() {
        return ((root, query, cb) -> StringUtils.hasText(nameLike)
                ? cb.like(cb.lower(root.get("name")), "%" + nameLike.toLowerCase() + "%")
                : null);
    }
}