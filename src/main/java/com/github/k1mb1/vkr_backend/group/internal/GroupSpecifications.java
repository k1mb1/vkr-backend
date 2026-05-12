package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import org.springframework.data.jpa.domain.Specification;

record GroupSpecifications(GroupFilter filter) {

    private static Specification<Group> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        var pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public Specification<Group> toSpec() {
        return nameContains(filter.name());
    }
}

