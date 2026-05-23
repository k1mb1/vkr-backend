package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.persistence.Specs;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

record SubjectSpecifications(SubjectFilter filter) {
    public Specification<Subject> toSpecification() {
        return Specs.<Subject>containsIgnoreCase("name", filter.name()).and(hasTeacherSpec());
    }

    private Specification<Subject> hasTeacherSpec() {
        return (root, query, cb) -> {
            if (filter.teacherId() == null) {
                return null;
            }
            Subquery<TeacherSubjectPermission> sub = query.subquery(TeacherSubjectPermission.class);
            Root<TeacherSubjectPermission> permission = sub.from(TeacherSubjectPermission.class);
            sub.select(permission.get("id")).where(
                cb.equal(permission.get("subject").get("id"), root.get("id")),
                cb.equal(permission.get("teacher").get("id"), filter.teacherId())
            );
            return cb.exists(sub);
        };
    }
}
