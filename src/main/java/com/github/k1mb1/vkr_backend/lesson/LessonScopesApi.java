package com.github.k1mb1.vkr_backend.lesson;

import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkAddLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkReplaceLessonScopesRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonScopeResponse;

import java.util.List;
import java.util.UUID;

public interface LessonScopesApi {
    List<LessonScopeResponse> addScopes(UUID lessonId, BulkAddLessonScopesRequest request);

    List<LessonScopeResponse> replaceScopesOfLesson(UUID lessonId, BulkReplaceLessonScopesRequest request);

    void deleteScope(UUID scopeId);
}
