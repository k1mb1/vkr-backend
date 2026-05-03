package com.github.k1mb1.vkr_backend.education.lessons.api;

import java.util.List;
import java.util.UUID;

public interface LessonQueryFacade {
    boolean existsById(UUID id);
    List<LessonInfo> findBySubjectId(UUID subjectId);
    List<LessonInfo> findBySubjectIdAndGroupId(UUID subjectId, UUID groupId);
    List<LessonInfo> findBySubjectIdAndType(UUID subjectId, LessonType type);
}
