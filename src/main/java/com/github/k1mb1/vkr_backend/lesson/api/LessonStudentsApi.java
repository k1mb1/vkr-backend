package com.github.k1mb1.vkr_backend.lesson.api;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Публичный порт модуля lesson: студенты в аудитории занятия или его проведений
 * (scope). Аудитория выводится из scope'ов: allGroups — все студенты групп
 * предмета, иначе группа/подгруппа, на которую ограничен scope. Результат
 * отсортирован по имени и не содержит дубликатов.
 */
public interface LessonStudentsApi {

    /** Студенты аудитории одного проведения. */
    List<LessonStudentResponse> studentsOfScope(UUID lessonScopeId);

    /** Объединение студентов по нескольким проведениям (без N+1 по группам). */
    List<LessonStudentResponse> studentsOfScopes(Collection<UUID> lessonScopeIds);
}
