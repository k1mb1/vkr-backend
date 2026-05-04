package com.github.k1mb1.vkr_backend.education.assignments.api;

import com.github.k1mb1.vkr_backend.education.assignments.internal.LessonTaskEntity;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record TaskFilter(String title, Boolean mandatory) {
    public Specification<LessonTaskEntity> toSpecification(UUID lessonId) {
        return Specification.where(lessonIdSpec(lessonId)).and(titleSpec()).and(mandatorySpec());
    }

    private Specification<LessonTaskEntity> lessonIdSpec(UUID lessonId) {
        return (root, query, cb) -> lessonId != null
            ? cb.equal(root.get("lessonId"), lessonId)
            : null;
    }

    private Specification<LessonTaskEntity> titleSpec() {
        return (root, query, cb) -> title != null && !title.isBlank()
            ? cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%")
            : null;
    }

    private Specification<LessonTaskEntity> mandatorySpec() {
        return (root, query, cb) -> mandatory != null
            ? cb.equal(root.get("isMandatory"), mandatory)
            : null;
    }
}
