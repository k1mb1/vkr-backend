package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.*;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LessonService
    implements LessonApi {

    final LessonRepository lessonRepository;

    final LessonMapper lessonMapper;

    final GroupReferenceService groupReferenceService;

    final SubjectRepository subjectRepository;

    final TeacherSubjectPermissionRepository permissionRepository;

    static LocalDate earliestStartedAt(Lesson lesson) {
        return lesson.getScopes()
            .stream()
            .map(LessonScope::getStartedAt)
            .filter(Objects::nonNull)
            .min(Comparator.naturalOrder())
            .orElse(null);
    }

    @Transactional
    @Override
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest request) {
        var lesson = lessonRepository.findWithDetailsById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        lessonMapper.updateEntity(request, lesson);

        if (request.subjectId() != null) {
            lesson.setSubject(subjectRepository.getReferenceById(request.subjectId()));
        }
        if (request.orderIndex() != null) {
            lesson.setOrderIndex(request.orderIndex());
        }

        var newScopes = request.scopes();
        if (newScopes != null) {
            if (newScopes.isEmpty()) {
                throw new IllegalArgumentException("scopes must be non-empty");
            }
            var builtScopes = buildLessonScopes(lesson, newScopes);
            lesson.getScopes().clear();
            lesson.getScopes().addAll(builtScopes);
        }

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    @Override
    public void deleteLesson(UUID id) {
        var lesson = lessonRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));
        lesson.archive();
        lessonRepository.save(lesson);
    }

    @Override
    public List<LessonResponse> getLessons(LessonFilter filter) {
        var permission = permissionRepository.findWithDetailsById(filter.permissionId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "TeacherSubjectPermission",
                filter.permissionId()
            ));
        var lessons = lessonRepository.findAll(LessonSpecifications.forPermission(permission));
        return lessons.stream()
            .sorted(Comparator.comparing(
                    LessonService::earliestStartedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
                )
                        .thenComparingInt(Lesson::getOrderIndex))
            .map(lessonMapper::toResponse)
            .toList();
    }

    @Transactional
    @Override
    public List<LessonResponse> bulkScheduleLessons(
        BulkScheduleRequest request
    ) {
        var subject = subjectRepository.findById(request.subjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        var audiences = resolveAudiences(request.audiences());
        var lessons = new ArrayList<Lesson>();

        var counters = nextOrderIndexByType(subject.getId());

        for (var entry : request.schedules()) {
            lessons.addAll(generateSchedule(subject, audiences, entry, counters));
        }

        assignDefaultTopics(lessons);

        return lessonRepository.saveAll(lessons).stream().map(lessonMapper::toResponse).toList();
    }

    @Transactional
    @Override
    public List<LessonResponse> createLessonsByType(
        CreateLessonsByTypeRequest request
    ) {
        var subject = subjectRepository.findById(request.subjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));
        var resolvedScopes = resolveScopes(request.scopes());
        var lessons = new ArrayList<Lesson>();
        var counters = nextOrderIndexByType(subject.getId());

        for (int i = 0; i < request.lectureCount(); i++) {
            lessons.add(lessonTemplate(
                subject,
                LessonType.LECTURE,
                counters.merge(LessonType.LECTURE, 1, Integer::sum),
                resolvedScopes
            ));
        }
        for (int i = 0; i < request.practiceCount(); i++) {
            lessons.add(lessonTemplate(
                subject,
                LessonType.PRACTICE,
                counters.merge(LessonType.PRACTICE, 1, Integer::sum),
                resolvedScopes
            ));
        }

        assignDefaultTopics(lessons);

        return lessonRepository.saveAll(lessons).stream().map(lessonMapper::toResponse).toList();
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

    private List<ResolvedScope> resolveScopes(List<LessonScopeRequest> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("scopes must be non-empty");
        }
        var seen = new HashSet<String>();
        var result = new ArrayList<ResolvedScope>();
        for (var req : scopes) {
            var key = req.allGroups() + "|" + req.groupId() + "|" + req.allowedSubgroupId() + "|" + req.startedAt();
            if (!seen.add(key)) {
                throw new IllegalArgumentException("Duplicate scope in request: groupId=" + req.groupId() + ", allowedSubgroupId=" + req.allowedSubgroupId() + ", startedAt=" + req.startedAt());
            }
            Group group = req.groupId() != null
                          ? groupReferenceService.getGroupReferenceById(req.groupId())
                          : null;
            Subgroup allowedSubgroup = req.allowedSubgroupId() != null
                                       ? groupReferenceService.getSubgroupReferenceById(req.allowedSubgroupId())
                                       : null;
            validateSubgroupBelongsToGroup(allowedSubgroup, group);
            result.add(new ResolvedScope(group, allowedSubgroup, req.startedAt(), req.allGroups()));
        }
        return result;
    }

    private List<ResolvedAudience> resolveAudiences(List<LessonAudienceRequest> audiences) {
        if (audiences == null || audiences.isEmpty()) {
            throw new IllegalArgumentException("audiences must be non-empty");
        }
        var seen = new HashSet<String>();
        var result = new ArrayList<ResolvedAudience>();
        for (var req : audiences) {
            var key = req.allGroups() + "|" + req.groupId() + "|" + req.allowedSubgroupId();
            if (!seen.add(key)) {
                throw new IllegalArgumentException("Duplicate audience in request: groupId=" + req.groupId() + ", allowedSubgroupId=" + req.allowedSubgroupId());
            }
            Group group = req.groupId() != null
                          ? groupReferenceService.getGroupReferenceById(req.groupId())
                          : null;
            Subgroup allowedSubgroup = req.allowedSubgroupId() != null
                                       ? groupReferenceService.getSubgroupReferenceById(req.allowedSubgroupId())
                                       : null;
            validateSubgroupBelongsToGroup(allowedSubgroup, group);
            result.add(new ResolvedAudience(group, allowedSubgroup, req.allGroups()));
        }
        return result;
    }

    private List<LessonScope> buildLessonScopes(Lesson lesson, List<LessonScopeRequest> requests) {
        var resolved = resolveScopes(requests);
        var result = new ArrayList<LessonScope>();
        for (var r : resolved) {
            result.add(LessonScope.builder()
                           .lesson(lesson)
                           .group(r.group())
                           .allowedSubgroup(r.allowedSubgroup())
                           .startedAt(r.startedAt())
                           .allGroups(r.allGroups())
                           .build());
        }
        return result;
    }

    private void validateSubgroupBelongsToGroup(Subgroup subgroup, Group group) {
        if (subgroup != null && group == null) {
            throw new IllegalArgumentException("Subgroup is set but group is null");
        }
        if (subgroup != null && !Objects.equals(subgroup.getGroup().getId(), group.getId())) {
            throw new IllegalArgumentException("Subgroup does not belong to the specified group");
        }
    }

    private List<Lesson> generateSchedule(
        Subject subject,
        List<ResolvedAudience> audiences,
        BulkScheduleRequest.Entry entry,
        Map<LessonType, Integer> counters
    ) {
        var lessons = new ArrayList<Lesson>();
        var patterns = entry.daysOfWeek();
        var weekStart = entry.startDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var weekIndex = 0;

        while (lessons.size() < entry.totalCount()) {
            var pattern = patterns.get(weekIndex % patterns.size());
            var sortedDays = pattern.stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .toList();

            for (var dow : sortedDays) {
                if (lessons.size() >= entry.totalCount()) {
                    break;
                }
                var lessonDate = weekStart.plusDays(dow.getValue() - DayOfWeek.MONDAY.getValue());
                if (!lessonDate.isBefore(entry.startDate())) {
                    var order = counters.merge(entry.type(), 1, Integer::sum);
                    lessons.add(lessonWithDateForAllAudiences(
                        subject,
                        entry.type(),
                        order,
                        audiences,
                        lessonDate
                    ));
                }
            }

            weekStart = weekStart.plusWeeks(1);
            weekIndex++;
        }

        return lessons;
    }

    private Lesson lessonTemplate(
        Subject subject,
        LessonType type,
        int orderIndex,
        List<ResolvedScope> scopes
    ) {
        var lesson = Lesson.builder().subject(subject).type(type).orderIndex(orderIndex).build();
        for (var s : scopes) {
            lesson.getScopes()
                .add(LessonScope.builder()
                         .lesson(lesson)
                         .group(s.group())
                         .allowedSubgroup(s.allowedSubgroup())
                         .startedAt(s.startedAt())
                         .allGroups(s.allGroups())
                         .build());
        }
        return lesson;
    }

    private Lesson lessonWithDateForAllAudiences(
        Subject subject,
        LessonType type,
        int orderIndex,
        List<ResolvedAudience> audiences,
        LocalDate startedAt
    ) {
        var lesson = Lesson.builder().subject(subject).type(type).orderIndex(orderIndex).build();
        for (var a : audiences) {
            lesson.getScopes()
                .add(LessonScope.builder()
                         .lesson(lesson)
                         .group(a.group())
                         .allowedSubgroup(a.allowedSubgroup())
                         .startedAt(startedAt)
                         .allGroups(a.allGroups())
                         .build());
        }
        return lesson;
    }

    private record ResolvedScope(
        Group group,

        Subgroup allowedSubgroup,

        LocalDate startedAt,

        boolean allGroups
    ) {}

    private record ResolvedAudience(
        Group group,

        Subgroup allowedSubgroup,

        boolean allGroups
    ) {}
}
