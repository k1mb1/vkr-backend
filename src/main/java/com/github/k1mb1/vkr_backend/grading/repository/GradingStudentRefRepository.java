package com.github.k1mb1.vkr_backend.grading.repository;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки модуля grading на студентов (group::domain) — reference для FK оценок. */
public interface GradingStudentRefRepository extends Repository<StudentEntity, UUID> {

    StudentEntity getReferenceById(UUID id);
}
