package com.github.k1mb1.vkr_backend.domain.student_attendances;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public record StudentAttendanceFilter(UUID lessonId, UUID studentId, PresenceType presence) {

    public Specification<StudentAttendanceEntity> toSpecification() {
        return Specification.where(lessonIdSpec())
                .and(studentIdSpec())
                .and(presenceSpec());
    }

    private Specification<StudentAttendanceEntity> lessonIdSpec() {
        return ((root, query, cb) -> lessonId != null
                ? cb.equal(root.get("lessonId"), lessonId)
                : null);
    }

    private Specification<StudentAttendanceEntity> studentIdSpec() {
        return ((root, query, cb) -> studentId != null
                ? cb.equal(root.get("studentId"), studentId)
                : null);
    }

    private Specification<StudentAttendanceEntity> presenceSpec() {
        return ((root, query, cb) -> presence != null
                ? cb.equal(root.get("presence"), presence)
                : null);
    }
}