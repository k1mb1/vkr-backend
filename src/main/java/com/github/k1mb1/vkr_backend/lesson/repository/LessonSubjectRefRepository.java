package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки модуля lesson на предметы (subject::domain) — только получение reference для FK. */
public interface LessonSubjectRefRepository extends Repository<SubjectEntity, UUID> {

    SubjectEntity getReferenceById(UUID id);

    java.util.Optional<SubjectEntity> findById(UUID id);
}
