package com.github.k1mb1.vkr_backend.education.lessons.internal;

import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record LessonFilter(UUID subjectId, LessonType lessonType, UUID groupId) {

    public Specification<LessonEntity> toSpecification() {
        return Specification.where(subjectIdSpec()).and(lessonTypeSpec()).and(groupIdSpec());
    }

    private Specification<LessonEntity> subjectIdSpec() {
        return (root, query, cb) -> subjectId != null ? cb.equal(root.get("subjectId"), subjectId) : null;
    }

    private Specification<LessonEntity> lessonTypeSpec() {
        return (root, query, cb) -> lessonType != null ? cb.equal(root.get("type"), lessonType) : null;
    }

    private Specification<LessonEntity> groupIdSpec() {
        return (root, query, cb) -> groupId != null ? cb.equal(root.get("groupId"), groupId) : null;
    }
}
