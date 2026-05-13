package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import org.springframework.data.jpa.domain.Specification;

record GroupSpecifications(GroupFilter filter) {
    public Specification<Group> toSpecification() {
        return nameContainsSpec();
    }

    private Specification<Group> nameContainsSpec() {
        return (root, query, cb) -> {
            if (filter.name() == null || filter.name().isBlank()) {
                return null;
            }
            var pattern = "%" + filter.name().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }
}
