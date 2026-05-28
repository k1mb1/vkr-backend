package com.github.k1mb1.vkr_backend.lesson;

import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;

import java.util.List;
import java.util.UUID;

public interface LessonApi {
    LessonResponse getLessonById(UUID id);

    LessonResponse updateLesson(UUID id, UpdateLessonRequest request);

    void deleteLesson(UUID id);

    List<LessonResponse> getLessons(LessonFilter filter);

    List<LessonResponse> bulkCreate(BulkCreateLessonsRequest request);
}
