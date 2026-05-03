package com.github.k1mb1.vkr_backend.education.structure.api;

import com.github.k1mb1.vkr_backend.education.structure.internal.StudentGroupEntity;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record StudentGroupFilter(String name) {
    public Specification<StudentGroupEntity> toSpecification() {
        return Specification.where(mainGroupSpec()).and(nameSpec());
    }
    private Specification<StudentGroupEntity> mainGroupSpec() {
        return (root, query, cb) -> cb.isNull(root.get("parentGroup"));
    }
    private Specification<StudentGroupEntity> nameSpec() {
        return (root, query, cb) -> name != null && !name.isBlank()
            ? cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
            : null;
    }
}
