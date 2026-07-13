package com.github.k1mb1.vkr_backend.group.specification;

import com.github.k1mb1.vkr_backend.common.persistence.Specs;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.service.dto.filter.GroupFilter;
import org.springframework.data.jpa.domain.Specification;

public record GroupSpecifications(GroupFilter filter) {
    public Specification<GroupEntity> toSpecification() {
        return Specs.containsIgnoreCase("name", filter.name());
    }
}
