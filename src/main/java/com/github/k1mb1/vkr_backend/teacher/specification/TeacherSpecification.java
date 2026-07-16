package com.github.k1mb1.vkr_backend.teacher.specification;

import com.github.k1mb1.vkr_backend.common.persistence.Specs;
import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import com.github.k1mb1.vkr_backend.teacher.service.dto.filter.TeacherFilter;
import org.springframework.data.jpa.domain.Specification;

public record TeacherSpecification(TeacherFilter filter) {
    public Specification<TeacherEntity> toSpecification() {
        return Specs.containsIgnoreCase("username", filter.username());
    }
}
