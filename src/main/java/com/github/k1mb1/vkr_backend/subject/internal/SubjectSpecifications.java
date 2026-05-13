package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectAssignment;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

record SubjectSpecifications(SubjectFilter filter) {
    public Specification<Subject> toSpecification() {
        return nameContainsSpec().and(hasTeacherSpec());
    }

    private Specification<Subject> nameContainsSpec() {
        return (root, query, cb) -> {
            if (filter.name() == null) {
                return null;
            }
            var pattern = "%" + filter.name().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    private Specification<Subject> hasTeacherSpec() {
        return (root, query, cb) -> {
            if (filter.teacherId() == null) {
                return null;
            }
            Subquery<SubjectAssignment> sub = query.subquery(SubjectAssignment.class);
            Root<SubjectAssignment> assignment = sub.from(SubjectAssignment.class);
            sub.select(assignment)
                .where(cb.equal(assignment.get("offering").get("subject").get("id"),
                                root.get("id")
                       ), cb.equal(assignment.get("teacher").get("id"), filter.teacherId())
                );
            return cb.exists(sub);
        };
    }
}
