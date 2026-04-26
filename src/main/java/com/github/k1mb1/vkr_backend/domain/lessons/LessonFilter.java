package com.github.k1mb1.vkr_backend.domain.lessons;

import java.util.UUID;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record LessonFilter(
    UUID subjectId
) {

    public Specification<LessonEntity> toSpecification() {
        return Specification.where(subjectIdSpec());
    }

    private Specification<LessonEntity> subjectIdSpec() {
        return (root, query, cb) ->
            subjectId != null
                ? cb.equal(root.get("subject").get("id"), subjectId)
                : null;
    }
}
