package com.github.k1mb1.vkr_backend.lesson;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import java.util.Collection;
import java.util.List;

public interface LessonStudentsApi {
    /**
     * Students in the audience of a lesson, derived from the union of all its scopes.
     * Result is sorted by username and contains no duplicates.
     */
    List<Student> studentsOf(Lesson lesson);

    /**
     * Students in the audience of a single lesson scope.
     * For an allGroups scope — all students of the subject's groups; otherwise the group/subgroup
     * the scope is restricted to.
     */
    List<Student> studentsOf(LessonScope scope);

    /**
     * Union of students across several scopes, sorted by username and de-duplicated.
     * Loads all required groups in a single query (no per-scope N+1).
     */
    List<Student> studentsOf(Collection<LessonScope> scopes);
}
