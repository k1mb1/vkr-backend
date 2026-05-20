package com.github.k1mb1.vkr_backend.lesson;

import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.student.domain.Student;

import java.util.List;

public interface LessonStudentsApi {
    /**
     * Students in the audience of a lesson, derived strictly from the lesson's scopes
     * (or from the subject's attached groups when lesson.allGroups=true).
     * Result is sorted by username and contains no duplicates.
     */
    List<Student> studentsOf(Lesson lesson);
}
