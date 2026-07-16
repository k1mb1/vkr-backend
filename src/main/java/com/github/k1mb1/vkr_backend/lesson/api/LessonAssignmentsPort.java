package com.github.k1mb1.vkr_backend.lesson.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SPI модуля lesson: задания занятий для встраивания в {@code LessonResponse}.
 * Реализуется модулем journal (владельцем заданий) — так journal зависит от
 * lesson, а не наоборот, и модульный граф остаётся однонаправленным.
 */
public interface LessonAssignmentsPort {

    List<LessonAssignmentResponse> assignmentsOfLesson(UUID lessonId);

    Map<UUID, List<LessonAssignmentResponse>> assignmentsOfLessons(Collection<UUID> lessonIds);
}
