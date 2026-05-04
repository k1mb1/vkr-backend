package com.github.k1mb1.vkr_backend.education.assignments.api;

import com.github.k1mb1.vkr_backend.education.assignments.internal.StudentTaskGradeEntity;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record GradeFilter(UUID studentId, SubmissionStatus status) {
    public Specification<StudentTaskGradeEntity> toSpecification(UUID taskId) {
        return Specification.where(taskIdSpec(taskId)).and(studentIdSpec()).and(statusSpec());
    }

    private Specification<StudentTaskGradeEntity> taskIdSpec(UUID taskId) {
        return (root, query, cb) -> taskId != null
            ? cb.equal(root.get("task").get("id"), taskId)
            : null;
    }

    private Specification<StudentTaskGradeEntity> studentIdSpec() {
        return (root, query, cb) -> studentId != null
            ? cb.equal(root.get("studentId"), studentId)
            : null;
    }

    private Specification<StudentTaskGradeEntity> statusSpec() {
        return (root, query, cb) -> status != null
            ? cb.equal(root.get("status"), status)
            : null;
    }
}
