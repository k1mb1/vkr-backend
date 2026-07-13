package com.github.k1mb1.vkr_backend.subject.specification;

import com.github.k1mb1.vkr_backend.common.persistence.Specs;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import com.github.k1mb1.vkr_backend.subject.service.dto.filter.SubjectFilter;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public record SubjectSpecifications(SubjectFilter filter) {
    public Specification<SubjectEntity> toSpecification() {
        return Specs.<SubjectEntity>containsIgnoreCase("name", filter.name()).and(hasTeacherSpec());
    }

    private Specification<SubjectEntity> hasTeacherSpec() {
        return (root, query, cb) -> {
            if (filter.teacherId() == null) {
                return null;
            }
            Subquery<TeacherSubjectPermissionEntity> sub = query.subquery(TeacherSubjectPermissionEntity.class);
            Root<TeacherSubjectPermissionEntity> permission = sub.from(TeacherSubjectPermissionEntity.class);
            sub.select(permission.get("id"))
                    .where(
                            cb.equal(permission.get("subject").get("id"), root.get("id")),
                            cb.equal(permission.get("teacher").get("id"), filter.teacherId()));
            return cb.exists(sub);
        };
    }
}
