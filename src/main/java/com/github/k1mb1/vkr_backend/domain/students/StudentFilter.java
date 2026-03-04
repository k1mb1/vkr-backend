package com.github.k1mb1.vkr_backend.domain.students;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public record StudentFilter(String nameLike, UUID groupId) {

    public Specification<StudentEntity> toSpecification() {
        return Specification.where(nameLikeSpec())
                .and(groupIdSpec());
    }

    private Specification<StudentEntity> nameLikeSpec() {
        return ((root, query, cb) -> StringUtils.hasText(nameLike)
                ? cb.like(cb.lower(root.get("username")), "%" + nameLike.toLowerCase() + "%")
                : null);
    }

    private Specification<StudentEntity> groupIdSpec() {
        return ((root, query, cb) -> groupId != null
                ? cb.equal(root.get("group").get("id"), groupId)
                : null);
    }
}