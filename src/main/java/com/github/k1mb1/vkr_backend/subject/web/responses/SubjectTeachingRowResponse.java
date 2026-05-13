package com.github.k1mb1.vkr_backend.subject.web.responses;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;

import java.util.UUID;

public record SubjectTeachingRowResponse(
    UUID offeringId,

    UUID groupId,

    String groupName,

    UUID assignmentId,

    UUID teacherId,

    String teacherName,

    LessonType lessonTypeScope,

    UUID subgroupId,

    Integer subgroupIndex
) {}