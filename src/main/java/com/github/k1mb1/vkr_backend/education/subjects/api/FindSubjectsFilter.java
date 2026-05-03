package com.github.k1mb1.vkr_backend.education.subjects.api;

import com.github.k1mb1.vkr_backend.education.subjects.internal.SubjectEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public record FindSubjectsFilter(Boolean archived) {
    public Specification<SubjectEntity> toServiceFilter(UUID teacherId) {
        return Specification.where(teacherIdSpec(teacherId)).and(archivedSpec());
    }
    private Specification<SubjectEntity> teacherIdSpec(UUID teacherId) {
        return (root, query, cb) -> teacherId != null
            ? cb.isMember(teacherId, root.get("teacherIds"))
            : null;
    }
    private Specification<SubjectEntity> archivedSpec() {
        return (root, query, cb) -> archived != null
            ? cb.equal(root.get("archived"), archived)
            : null;
    }
}
