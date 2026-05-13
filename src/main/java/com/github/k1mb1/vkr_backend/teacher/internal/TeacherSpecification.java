package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import com.github.k1mb1.vkr_backend.teacher.web.filters.TeacherFilter;
import org.springframework.data.jpa.domain.Specification;

record TeacherSpecification(TeacherFilter filter) {
    public Specification<Teacher> toSpecification() {
        return usernameContainsSpec();
    }

    private Specification<Teacher> usernameContainsSpec() {
        return (root, query, cb) -> {
            if (filter.username() == null) {
                return null;
            }
            var pattern = "%" + filter.username().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("username")), pattern);
        };
    }
}
