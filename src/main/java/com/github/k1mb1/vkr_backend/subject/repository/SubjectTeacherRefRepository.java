package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки модуля subject на преподавателей (teacher::domain) — reference для FK прав. */
public interface SubjectTeacherRefRepository extends Repository<TeacherEntity, UUID> {

    TeacherEntity getReferenceById(UUID id);
}
