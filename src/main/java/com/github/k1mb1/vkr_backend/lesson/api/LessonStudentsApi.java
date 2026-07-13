package com.github.k1mb1.vkr_backend.lesson.api;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import java.util.Collection;
import java.util.List;

public interface LessonStudentsApi {
    /**
     * Students in the audience of a lesson, derived from the union of all its scopes.
     * Result is sorted by username and contains no duplicates.
     */
    List<StudentEntity> studentsOf(LessonEntity lesson);

    /**
     * Students in the audience of a single lesson scope.
     * For an allGroups scope — all students of the subject's groups; otherwise the group/subgroup
     * the scope is restricted to.
     */
    List<StudentEntity> studentsOf(LessonScopeEntity scope);

    /**
     * Union of students across several scopes, sorted by username and de-duplicated.
     * Loads all required groups in a single query (no per-scope N+1).
     */
    List<StudentEntity> studentsOf(Collection<LessonScopeEntity> scopes);
}
