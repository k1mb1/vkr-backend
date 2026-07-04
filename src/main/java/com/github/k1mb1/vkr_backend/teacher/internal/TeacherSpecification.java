package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.common.persistence.Specs;
import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import com.github.k1mb1.vkr_backend.teacher.web.filters.TeacherFilter;
import org.springframework.data.jpa.domain.Specification;

record TeacherSpecification(TeacherFilter filter) {
    Specification<Teacher> toSpecification() {
        return Specs.containsIgnoreCase("username", filter.username());
    }
}
