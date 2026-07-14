package com.github.k1mb1.vkr_backend.journal.repository;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки модуля journal на студентов (group::domain) — reference для FK отметок и оценок. */
public interface JournalStudentRefRepository extends Repository<StudentEntity, UUID> {

    StudentEntity getReferenceById(UUID id);
}
