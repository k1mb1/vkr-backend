package com.github.k1mb1.vkr_backend.teacher.repository;

import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки teacher на предметы (subject::domain) — reference для привязки права к предмету. */
public interface TeacherSubjectRefRepository extends Repository<SubjectEntity, UUID> {

    SubjectEntity getReferenceById(UUID id);
}
