package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.common.persistence.Specs;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import org.springframework.data.jpa.domain.Specification;

record GroupSpecifications(GroupFilter filter) {
    public Specification<Group> toSpecification() {
        return Specs.containsIgnoreCase("name", filter.name());
    }
}
