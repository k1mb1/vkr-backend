package com.github.k1mb1.vkr_backend.domain.subjects;

import java.util.UUID;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherEntity;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;

@Builder
public record SubjectFilter(
		UUID teacherId,
		Boolean archived
) {

	public Specification<SubjectEntity> toSpecification() {
		return Specification.where(teacherIdSpec()).and(archivedSpec());
	}

	private Specification<SubjectEntity> teacherIdSpec() {
		return (root, query, cb) -> teacherId != null
			? cb.equal(root.join("teachers").get("id"), teacherId)
			: null;
	}

	private Specification<SubjectEntity> archivedSpec() {
		return (root, query, cb) -> archived != null
			? cb.equal(root.get("archived"), archived)
			: null;
	}
}
