package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.grading.GradingApi;
import com.github.k1mb1.vkr_backend.grading.web.responses.AssignmentResponse;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.LessonScopesApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.LessonScopeAudienceRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
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
class LessonService implements LessonApi {

    final LessonRepository lessonRepository;

    final LessonMapper lessonMapper;

    final SubjectRepository subjectRepository;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonScopesApi lessonScopesApi;

    final GradingApi gradingApi;

    final GroupReferenceService groupReferenceService;

    static @Nullable LocalDate earliestStartedAt(Lesson lesson) {
        return lesson.getScopes().stream()
                .map(LessonScope::getStartedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    @PreAuthorize("@authz.canAccessLesson(#id)")
    public LessonResponse getLessonById(UUID id) {
        var lesson =
                lessonRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        var assignments = gradingApi.getAssignmentsByLesson(id);
        return lessonMapper.toResponse(lesson, lesson.getScopes().stream().toList(), assignments);
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canAccessLesson(#id)")
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest request) {
        var lesson =
                lessonRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

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

        return getLessonById(id);
    }

    @Transactional
    @Override
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
    @Override
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

    @Override
    @PreAuthorize("@authz.ownsPermission(#filter.permissionId())")
    public List<LessonResponse> getLessons(LessonFilter filter) {
        var permission = permissionRepository
                .findWithDetailsById(filter.permissionId())
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", filter.permissionId()));
        var lessons = lessonRepository.findAllWithDetails(LessonSpecifications.forPermission(permission));
        var sorted = lessons.stream()
                .sorted(Comparator.comparing(
                                LessonService::earliestStartedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingInt(Lesson::getOrderIndex))
                .toList();
        var assignmentsByLesson = gradingApi.getAssignmentsByLessons(
                sorted.stream().map(Lesson::getId).toList());
        return sorted.stream()
                .map(lesson -> lessonMapper.toResponse(
                        lesson,
                        LessonSpecifications.visibleScopes(lesson, permission),
                        assignmentsByLesson.getOrDefault(lesson.getId(), List.of())))
                .toList();
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canAccessSubject(#request.subjectId())")
    public List<LessonResponse> bulkCreate(BulkCreateLessonsRequest request) {
        var subject = subjectRepository
                .findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        var counters = nextOrderIndexByType(subject.getId());
        var lessons = new ArrayList<Lesson>();

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
                .map(lesson -> lessonMapper.toResponse(lesson, List.<LessonScope>of(), List.<AssignmentResponse>of()))
                .toList();
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canAccessSubject(#request.subjectId())")
    public List<LessonResponse> bulkSchedule(BulkScheduleLessonsRequest request) {
        var subject = subjectRepository
                .findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));

        // Для каждого item — своя серия из count дат (count общий для всех).
        var datesPerItem = request.items().stream()
                .map(item -> schedule(item.firstLessonDate(), request.count(), item.days()))
                .toList();

        // count занятий: занятие k проводится на k-ю дату каждого item —
        // по проведению (scope) для аудитории этого item на его k-ю дату.
        var counters = nextOrderIndexByType(subject.getId());
        var lessons = new ArrayList<Lesson>(request.count());
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
                        lesson, lesson.getScopes().stream().toList(), List.<AssignmentResponse>of()))
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

    private LessonScope buildScope(Lesson lesson, LocalDate startedAt, LessonScopeAudienceRequest audience) {
        var scope = LessonScope.builder().lesson(lesson).startedAt(startedAt).build();
        if (audience == null) {
            scope.setAllGroups(true);
            return scope;
        }
        var ref = groupReferenceService.resolveAudience(audience.groupId(), audience.allowedSubgroupId());
        scope.setAllGroups(false);
        scope.setGroup(ref.group());
        scope.setAllowedSubgroup(ref.allowedSubgroup());
        return scope;
    }

    private Lesson lessonTemplate(UUID subjectId, LessonType type, int orderIndex) {
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
            map.put(type, max == null ? 0 : max);
        }
        return map;
    }
}
