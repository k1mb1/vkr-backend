package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Read-only доступ модуля lesson к ростерам студентов (сущность модуля group,
 * опубликованная через его {@code ::domain} named interface) — только чтение
 * для вычисления аудитории занятий; мутации ростера остаются в модуле group.
 */
public interface LessonStudentRepository extends Repository<StudentEntity, UUID> {

    List<StudentEntity> findByGroupIdInAndArchivedAtIsNull(Collection<UUID> groupIds);
}
