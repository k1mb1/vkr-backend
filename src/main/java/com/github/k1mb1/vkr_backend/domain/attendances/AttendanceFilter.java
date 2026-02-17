package com.github.k1mb1.vkr_backend.domain.attendances;

import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public record AttendanceFilter(UUID lessonId, UUID studentId, PresenceType presence) {

    public Specification<AttendanceEntity> toSpecification() {
        return Specification.where(lessonIdSpec())
                .and(studentIdSpec())
                .and(presenceSpec());
    }

    private Specification<AttendanceEntity> lessonIdSpec() {
        return ((root, query, cb) -> lessonId != null
                ? cb.equal(root.get("lessonId"), lessonId)
                : null);
    }

    private Specification<AttendanceEntity> studentIdSpec() {
        return ((root, query, cb) -> studentId != null
                ? cb.equal(root.get("studentId"), studentId)
                : null);
    }

    private Specification<AttendanceEntity> presenceSpec() {
        return ((root, query, cb) -> presence != null
                ? cb.equal(root.get("presence"), presence)
                : null);
    }
}