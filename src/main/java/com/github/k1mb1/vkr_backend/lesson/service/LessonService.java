package com.github.k1mb1.vkr_backend.lesson.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.lesson.api.LessonAssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonAssignmentsPort;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.mapper.LessonMapper;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonGroupRefRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonPermissionRefRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonSubgroupRefRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonSubjectRefRepository;
import com.github.k1mb1.vkr_backend.lesson.service.dto.filter.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.LessonScopeAudienceRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.response.LessonResponse;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.LessonType;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    final LessonRepository lessonRepository;

    final LessonMapper lessonMapper;

    final LessonSubjectRefRepository subjectRefRepository;

    final LessonPermissionRefRepository permissionRefRepository;

    final LessonScopeService lessonScopeService;

    final LessonAssignmentsPort lessonAssignmentsPort;

    final LessonGroupRefRepository groupRefRepository;

    final LessonSubgroupRefRepository subgroupRefRepository;

    static @Nullable LocalDate earliestStartedAt(LessonEntity lesson) {
        return lesson.getScopes().stream()
                .map(LessonScopeEntity::getStartedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @PreAuthorize("@authz.canAccessLesson(#id)")
    public LessonResponse getLessonById(UUID id) {
        var lesson =
                lessonRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        var assignments = lessonAssignmentsPort.assignmentsOfLesson(id);
        return lessonMapper.toResponse(lesson, lesson.getScopes().stream().toList(), assignments);
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#id)")
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest request) {
        var lesson =
                lessonRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        if (request.header() != null) {
            var header = request.header();
            lessonMapper.updateEntity(header, lesson);
            if (header.subjectId() != null) {
                lesson.setSubject(subjectRefRepository.getReferenceById(header.subjectId()));
            }
            if (header.orderIndex() != null) {
                lesson.setOrderIndex(header.orderIndex());
            }
            lessonRepository.save(lesson);
        }

        if (request.scopes() != null) {
            lessonScopeService.replaceScopesOfLesson(id, request.scopes());
        }

        return getLessonById(id);
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#id)")
    public LessonResponse setActive(UUID id, boolean active) {
        var lesson = lessonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        if (active) {
            // Не более одного активного занятия на (предмет, тип) — снимаем флаг с остальных того же типа.
            lessonRepository.clearActiveForSubjectAndType(lesson.getSubject().getId(), lesson.getType());
            lessonRepository.flush();
        }
        lesson.setActive(active);
        lessonRepository.save(lesson);

        return getLessonById(id);
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLesson(#id)")
    public void deleteLesson(UUID id) {
        var lesson = lessonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        var subjectId = lesson.getSubject().getId();
        var type = lesson.getType();
        var removedIndex = lesson.getOrderIndex();

        lesson.archive();
        lessonRepository.save(lesson);
        lessonRepository.flush();

        lessonRepository.shiftOrderIndexDown(subjectId, type, removedIndex);
    }

    @PreAuthorize("@authz.ownsPermission(#filter.permissionId())")
    public List<LessonResponse> getLessons(LessonFilter filter) {
        var permission = permissionRefRepository
                .findWithDetailsById(filter.permissionId())
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", filter.permissionId()));
        var lessons = lessonRepository.findAll(LessonSpecifications.forPermission(permission));
        var sorted = lessons.stream()
                .sorted(Comparator.comparing(
                                LessonService::earliestStartedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(LessonEntity::getOrderIndex))
                .toList();
        var assignmentsByLesson = lessonAssignmentsPort.assignmentsOfLessons(
                sorted.stream().map(LessonEntity::getId).toList());
        return sorted.stream()
                .map(lesson -> lessonMapper.toResponse(
                        lesson,
                        LessonSpecifications.visibleScopes(lesson, permission),
                        assignmentsByLesson.getOrDefault(lesson.getId(), List.of())))
                .toList();
    }

    @Transactional
    @PreAuthorize("@authz.canAccessSubject(#request.subjectId())")
    public List<LessonResponse> bulkCreate(BulkCreateLessonsRequest request) {
        var subject = subjectRefRepository
                .findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        var counters = nextOrderIndexByType(subject.getId());
        var lessons = new ArrayList<LessonEntity>();

        for (int i = 0; i < request.lectureCount(); i++) {
            lessons.add(lessonTemplate(
                    subject.getId(), LessonType.LECTURE, counters.merge(LessonType.LECTURE, 1, Integer::sum)));
        }
        for (int i = 0; i < request.practiceCount(); i++) {
            lessons.add(lessonTemplate(
                    subject.getId(), LessonType.PRACTICE, counters.merge(LessonType.PRACTICE, 1, Integer::sum)));
        }

        assignDefaultTopics(lessons);

        return lessonRepository.saveAll(lessons).stream()
                .map(lesson -> lessonMapper.toResponse(
                        lesson, List.<LessonScopeEntity>of(), List.<LessonAssignmentResponse>of()))
                .toList();
    }

    @Transactional
    @PreAuthorize("@authz.canAccessSubject(#request.subjectId())")
    public List<LessonResponse> bulkSchedule(BulkScheduleLessonsRequest request) {
        var subject = subjectRefRepository
                .findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));

        // Для каждого item — своя серия из count дат (count общий для всех).
        var datesPerItem = request.items().stream()
                .map(item -> schedule(item.firstLessonDate(), request.count(), item.days()))
                .toList();

        // count занятий: занятие k проводится на k-ю дату каждого item —
        // по проведению (scope) для аудитории этого item на его k-ю дату.
        var counters = nextOrderIndexByType(subject.getId());
        var lessons = new ArrayList<LessonEntity>(request.count());
        for (int k = 0; k < request.count(); k++) {
            var lesson = lessonTemplate(
                    subject.getId(), request.lessonType(), counters.merge(request.lessonType(), 1, Integer::sum));
            for (int i = 0; i < request.items().size(); i++) {
                var audience = request.items().get(i).audience();
                var date = datesPerItem.get(i).get(k);
                lesson.getScopes().add(buildScope(lesson, date, audience));
            }
            lessons.add(lesson);
        }
        assignDefaultTopics(lessons);

        return lessonRepository.saveAll(lessons).stream()
                .map(lesson -> lessonMapper.toResponse(
                        lesson, lesson.getScopes().stream().toList(), List.<LessonAssignmentResponse>of()))
                .toList();
    }

    /**
     * Даты пар по недельному шаблону: первая пара = firstLessonDate, затем шаблон зацикливается
     * по неделям, пока не наберётся count дат. Внешний список — недели, внутренний — дни недели.
     */
    static List<LocalDate> schedule(LocalDate firstLessonDate, int count, List<List<DayOfWeek>> weeks) {
        // Понедельник недели, в которой стоит первая пара, — точка отсчёта.
        var weekStart = firstLessonDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var dates = new ArrayList<LocalDate>(count);
        for (int w = 0; dates.size() < count; w++) {
            var days = weeks.get(w % weeks.size()).stream().sorted().toList();
            for (var day : days) {
                if (dates.size() == count) {
                    break;
                }
                dates.add(weekStart.plusWeeks(w).with(day));
            }
        }
        return dates;
    }

    private LessonScopeEntity buildScope(
            LessonEntity lesson, LocalDate startedAt, LessonScopeAudienceRequest audience) {
        var scope =
                LessonScopeEntity.builder().lesson(lesson).startedAt(startedAt).build();
        if (audience == null) {
            scope.setAllGroups(true);
            return scope;
        }
        scope.setAllGroups(false);
        if (audience.groupId() != null) {
            scope.setGroup(groupRefRepository.getReferenceById(audience.groupId()));
            scope.setAllowedSubgroup(
                    subgroupRefRepository.resolveAllowedSubgroup(audience.allowedSubgroupId(), audience.groupId()));
        }
        return scope;
    }

    private LessonEntity lessonTemplate(UUID subjectId, LessonType type, int orderIndex) {
        return LessonEntity.builder()
                .subject(subjectRefRepository.getReferenceById(subjectId))
                .type(type)
                .orderIndex(orderIndex)
                .build();
    }

    private void assignDefaultTopics(List<LessonEntity> lessons) {
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
            map.put(type, max == null ? 0 : max);
        }
        return map;
    }
}
