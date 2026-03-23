package com.github.k1mb1.vkr_backend.domain.student_grades;

import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public record StudentGradeFilter(UUID lessonId, UUID studentId) {
    public Specification<StudentGradeEntity> toSpecification() {
        return Specification.where(lessonIdSpec()).and(studentIdSpec());
    }

    private Specification<StudentGradeEntity> lessonIdSpec() {
        return (
            (root, query, cb) ->
                lessonId != null
                    ? cb.equal(root.get("lessonId"), lessonId)
                    : null
        );
    }

    private Specification<StudentGradeEntity> studentIdSpec() {
        return (
            (root, query, cb) ->
                studentId != null
                    ? cb.equal(root.get("studentId"), studentId)
                    : null
        );
    }
}
