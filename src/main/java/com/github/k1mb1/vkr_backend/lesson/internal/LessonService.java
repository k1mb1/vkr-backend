package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.LessonScopesApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LessonService
    implements LessonApi {

    final LessonRepository lessonRepository;

    final LessonMapper lessonMapper;

    final SubjectRepository subjectRepository;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonScopesApi lessonScopesApi;

    final GradingApi gradingApi;

    static LocalDate earliestStartedAt(Lesson lesson) {
        return lesson.getScopes()
            .stream()
            .map(LessonScope::getStartedAt)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElse(null);
    }

    @Override
    public LessonResponse getLessonById(UUID id) {
        var lesson = lessonRepository.findWithDetailsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        var assignments = gradingApi.getAssignmentsByLesson(id);
        return lessonMapper.toResponse(lesson, lesson.getScopes().stream().toList(), assignments);
    }

    @Transactional
    @Override
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest request) {
        var lesson = lessonRepository.findWithDetailsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        if (request.header() != null) {
            var header = request.header();
            lessonMapper.updateEntity(header, lesson);
            if (header.subjectId() != null) {
                lesson.setSubject(subjectRepository.getReferenceById(header.subjectId()));
            }
            if (header.orderIndex() != null) {
                lesson.setOrderIndex(header.orderIndex());
            }
            lessonRepository.save(lesson);
        }

        if (request.scopes() != null) {
            lessonScopesApi.replaceScopesOfLesson(id, request.scopes());
        }

        if (request.assignments() != null) {
            gradingApi.updateAssignmentsOfLesson(id, request.assignments());
        }

        return getLessonById(id);
    }

    @Transactional
    @Override
    public void deleteLesson(UUID id) {
        var lesson = lessonRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        var subjectId = lesson.getSubject().getId();
        var type = lesson.getType();
        var removedIndex = lesson.getOrderIndex();

        lesson.archive();
        lessonRepository.save(lesson);
        lessonRepository.flush();

        lessonRepository.shiftOrderIndexDown(subjectId, type, removedIndex);
    }

    @Override
    public List<LessonResponse> getLessons(LessonFilter filter) {
        var permission = permissionRepository.findWithDetailsById(filter.permissionId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "TeacherSubjectPermission",
                filter.permissionId()
            ));
        var lessons = lessonRepository.findAll(LessonSpecifications.forPermission(permission));
        var sorted = lessons.stream()
            .sorted(Comparator.comparing(
                    LessonService::earliestStartedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
                )
                        .thenComparingInt(Lesson::getOrderIndex))
            .toList();
        var assignmentsByLesson = gradingApi.getAssignmentsByLessons(
            sorted.stream().map(Lesson::getId).toList()
        );
        return sorted.stream()
            .map(lesson -> lessonMapper.toResponse(
                lesson,
                LessonSpecifications.visibleScopes(lesson, permission),
                assignmentsByLesson.getOrDefault(lesson.getId(), List.of())
            ))
            .toList();
    }

    @Transactional
    @Override
    public List<LessonResponse> bulkCreate(BulkCreateLessonsRequest request) {
        var subject = subjectRepository.findById(request.subjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        var counters = nextOrderIndexByType(subject.getId());
        var lessons = new ArrayList<Lesson>();

        for (int i = 0; i < request.lectureCount(); i++) {
            lessons.add(lessonTemplate(
                subject.getId(),
                LessonType.LECTURE,
                counters.merge(LessonType.LECTURE, 1, Integer::sum)
            ));
        }
        for (int i = 0; i < request.practiceCount(); i++) {
            lessons.add(lessonTemplate(
                subject.getId(),
                LessonType.PRACTICE,
                counters.merge(LessonType.PRACTICE, 1, Integer::sum)
            ));
        }

        assignDefaultTopics(lessons);

        return lessonRepository.saveAll(lessons).stream()
            .map(lesson -> lessonMapper.toResponse(lesson, List.<LessonScope>of(), List.<AssignmentResponse>of()))
            .toList();
    }

    private Lesson lessonTemplate(
        UUID subjectId,
        LessonType type,
        int orderIndex
    ) {
        return Lesson.builder()
            .subject(subjectRepository.getReferenceById(subjectId))
            .type(type)
            .orderIndex(orderIndex)
            .build();
    }

    private void assignDefaultTopics(List<Lesson> lessons) {
        for (var lesson : lessons) {
            if (lesson.getTopic() != null && !lesson.getTopic().isBlank()) {
                continue;
            }
            switch (lesson.getType()) {
                case LECTURE -> lesson.setTopic("Лекция " + lesson.getOrderIndex());
                case PRACTICE -> lesson.setTopic("Практика " + lesson.getOrderIndex());
            }
        }
    }

    private Map<LessonType, Integer> nextOrderIndexByType(UUID subjectId) {
        var map = new EnumMap<LessonType, Integer>(LessonType.class);
        for (var type : LessonType.values()) {
            var max = lessonRepository.findMaxOrderIndex(subjectId, type);
            map.put(
                type,
                max == null
                ? 0
                : max
            );
        }
        return map;
    }
}
