package com.github.k1mb1.vkr_backend.domain.grades;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public record GradeFilter(UUID lessonId, UUID studentId) {

    public Specification<GradeEntity> toSpecification() {
        return Specification.where(lessonIdSpec())
                .and(studentIdSpec());
    }

    private Specification<GradeEntity> lessonIdSpec() {
        return ((root, query, cb) -> lessonId != null
                ? cb.equal(root.get("lessonId"), lessonId)
                : null);
    }

    private Specification<GradeEntity> studentIdSpec() {
        return ((root, query, cb) -> studentId != null
                ? cb.equal(root.get("studentId"), studentId)
                : null);
    }
}