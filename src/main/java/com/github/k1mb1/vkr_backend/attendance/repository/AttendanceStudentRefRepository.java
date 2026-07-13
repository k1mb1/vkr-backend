package com.github.k1mb1.vkr_backend.attendance.repository;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/** Ссылки модуля attendance на студентов (group::domain) — reference для FK отметок. */
public interface AttendanceStudentRefRepository extends Repository<StudentEntity, UUID> {

    StudentEntity getReferenceById(UUID id);
}
