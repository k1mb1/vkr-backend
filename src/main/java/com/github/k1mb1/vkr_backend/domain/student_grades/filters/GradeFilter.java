package com.github.k1mb1.vkr_backend.domain.student_grades.filters;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import com.github.k1mb1.vkr_backend.domain.student_grades.StudentTaskGradeEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification filter for {@link StudentTaskGradeEntity}.
 *
 * @param subjectId  Required. Filters grades by subject through task → lesson → subject.
 * @param lessonType Optional. Filters by lesson type (LECTURE, PRACTICE, NONE).
 * @param groupId    Optional. Filters by group/subgroup attached to the lesson.
 */
public record GradeFilter(
    UUID subjectId,
    LessonType lessonType,
    UUID groupId
) {

    public Specification<StudentTaskGradeEntity> toSpecification() {
        return Specification.where(subjectIdSpec()).and(lessonTypeSpec()).and(groupIdSpec());
    }

    private Specification<StudentTaskGradeEntity> subjectIdSpec() {
        return (root, query, cb) -> subjectId != null
            ? cb.equal(root.get("task").get("lesson").get("subject").get("id"), subjectId)
            : null;
    }

    private Specification<StudentTaskGradeEntity> lessonTypeSpec() {
        return (root, query, cb) -> lessonType != null
            ? cb.equal(root.get("task").get("lesson").get("type"), lessonType)
            : null;
    }

    private Specification<StudentTaskGradeEntity> groupIdSpec() {
        return (root, query, cb) -> groupId != null
            ? cb.equal(root.get("task").get("lesson").get("group").get("id"), groupId)
            : null;
    }
}
