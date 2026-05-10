package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.web.filters.SubjectFilter;
import org.springframework.data.jpa.domain.Specification;

record SubjectSpecifications(SubjectFilter filter) {

    public Specification<Subject> toSpec() {
        return nameContains(filter.name());
    }

    private static Specification<Subject> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        var pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), pattern);
    }
}

