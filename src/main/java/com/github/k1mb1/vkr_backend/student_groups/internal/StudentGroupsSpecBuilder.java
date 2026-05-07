package com.github.k1mb1.vkr_backend.student_groups.internal;

import com.github.k1mb1.vkr_backend.student_groups.domain.StudentGroup;
import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
class StudentGroupsSpecBuilder {

    Specification<StudentGroup> build(StudentGroupFilterRequest request) {
        return Specification.where(rootGroupsOnly()).and(
            nameLikeSpec(request.name())
        );
    }

    private Specification<StudentGroup> rootGroupsOnly() {
        return (root, query, cb) -> cb.isNull(root.get("parentGroup"));
    }

    private Specification<StudentGroup> nameLikeSpec(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(
                cb.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"
            );
        };
    }
}
