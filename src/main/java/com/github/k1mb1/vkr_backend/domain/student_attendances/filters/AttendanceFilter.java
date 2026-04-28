package com.github.k1mb1.vkr_backend.domain.student_attendances.filters;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonType;
import com.github.k1mb1.vkr_backend.domain.student_attendances.StudentAttendanceEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification filter for {@link StudentAttendanceEntity}.
 *
 * @param subjectId  Required. Filters attendance by subject through lesson → subject.
 * @param lessonType Optional. Filters by lesson type (LECTURE, PRACTICE, NONE).
 * @param groupId    Optional. Filters by group/subgroup attached to the lesson.
 */
public record AttendanceFilter(
    UUID subjectId,
    LessonType lessonType,
    UUID groupId
) {

    public Specification<StudentAttendanceEntity> toSpecification() {
        return Specification.where(subjectIdSpec()).and(lessonTypeSpec()).and(groupIdSpec());
    }

    private Specification<StudentAttendanceEntity> subjectIdSpec() {
        return (root, query, cb) -> subjectId != null
            ? cb.equal(root.get("lesson").get("subject").get("id"), subjectId)
            : null;
    }

    private Specification<StudentAttendanceEntity> lessonTypeSpec() {
        return (root, query, cb) -> lessonType != null
            ? cb.equal(root.get("lesson").get("type"), lessonType)
            : null;
    }

    private Specification<StudentAttendanceEntity> groupIdSpec() {
        return (root, query, cb) -> groupId != null
            ? cb.equal(root.get("lesson").get("group").get("id"), groupId)
            : null;
    }
}
