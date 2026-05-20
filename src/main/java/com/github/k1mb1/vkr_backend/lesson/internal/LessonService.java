package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.LessonScopeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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

    @Transactional
    @Override
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest request) {
        var lesson = lessonRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));

        lessonMapper.updateEntity(request, lesson);

        if (request.subjectId() != null) {
            lesson.setSubject(subjectRepository.getReferenceById(request.subjectId()));
        }

        var newAllGroups = request.allGroups();
        var newScopes = request.scopes();

        if (newAllGroups != null) {
            lesson.setAllGroups(newAllGroups);
        }
        if (newScopes != null) {
            boolean effectiveAllGroups = newAllGroups != null
                                         ? newAllGroups
                                         : lesson.isAllGroups();
            if (!effectiveAllGroups && newScopes.isEmpty()) {
                throw new IllegalArgumentException("scopes must be non-empty when allGroups=false");
            }
            var builtScopes = buildLessonScopes(lesson, newScopes);
            lesson.getScopes().clear();
            lesson.getScopes().addAll(builtScopes);
        } else if (newAllGroups != null && !newAllGroups && lesson.getScopes().isEmpty()) {
            throw new IllegalArgumentException("scopes must be non-empty when allGroups=false");
        }

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    @Override
    public void deleteLesson(UUID id) {
        var lesson = lessonRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
        lesson.archive();
        lessonRepository.save(lesson);
    }

    @Override
    public List<LessonResponse> getLessons(LessonFilter filter) {
        var permission = permissionRepository.findByIdWithDetails(filter.permissionId())
            .orElseThrow(() -> new EntityNotFoundException("TeacherSubjectPermission not found: " + filter.permissionId()));
        return lessonRepository.findAll(
                LessonSpecifications.forPermission(permission),
                Sort.by("startedAt")
            )
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    @Transactional
    @Override
    public List<LessonResponse> bulkScheduleLessons(
        BulkScheduleRequest request
    ) {
        var subject = subjectRepository.findById(request.subjectId())
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + request.subjectId()));
        var resolvedScopes = resolveScopes(request.allGroups(), request.scopes());
        var lessons = new ArrayList<Lesson>();

        for (var entry : request.schedules()) {
            lessons.addAll(generateSchedule(subject, request.allGroups(), resolvedScopes, entry));
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
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + request.subjectId()));
        var resolvedScopes = resolveScopes(request.allGroups(), request.scopes());
        var today = LocalDate.now();
        var lessons = new ArrayList<Lesson>();

        for (int i = 0; i < request.lectureCount(); i++) {
            lessons.add(lesson(
                subject,
                request.allGroups(),
                resolvedScopes,
                LessonType.LECTURE,
                today
            ));
        }
        for (int i = 0; i < request.practiceCount(); i++) {
            lessons.add(lesson(
                subject,
                request.allGroups(),
                resolvedScopes,
                LessonType.PRACTICE,
                today
            ));
        }

        assignDefaultTopics(lessons);

        return lessonRepository.saveAll(lessons).stream().map(lessonMapper::toResponse).toList();
    }

    private void assignDefaultTopics(List<Lesson> lessons) {
        int lectureCounter = 1;
        int practiceCounter = 1;
        for (var lesson : lessons) {
            switch (lesson.getType()) {
                case LECTURE -> lesson.setTopic("Лекция " + lectureCounter++);
                case PRACTICE -> lesson.setTopic("Практика " + practiceCounter++);
            }
        }
    }

    private List<ResolvedScope> resolveScopes(Boolean allGroups, List<LessonScopeRequest> scopes) {
        if (Boolean.TRUE.equals(allGroups)) {
            return List.of();
        }
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("scopes must be non-empty when allGroups=false");
        }
        var seen = new HashSet<String>();
        var result = new ArrayList<ResolvedScope>();
        for (var req : scopes) {
            var key = req.groupId() + "|" + req.allowedSubgroupId();
            if (!seen.add(key)) {
                throw new IllegalArgumentException("Duplicate scope in request: groupId=" + req.groupId() + ", allowedSubgroupId=" + req.allowedSubgroupId());
            }
            var group = groupReferenceService.getGroupReferenceById(req.groupId());
            Subgroup allowedSubgroup = req.allowedSubgroupId() != null
                                       ? groupReferenceService.getSubgroupReferenceById(req.allowedSubgroupId())
                                       : null;
            validateSubgroupBelongsToGroup(allowedSubgroup, group);
            result.add(new ResolvedScope(group, allowedSubgroup));
        }
        return result;
    }

    private List<LessonScope> buildLessonScopes(Lesson lesson, List<LessonScopeRequest> requests) {
        var resolved = resolveScopes(Boolean.FALSE, requests);
        var result = new ArrayList<LessonScope>();
        for (var r : resolved) {
            result.add(LessonScope.builder()
                           .lesson(lesson)
                           .group(r.group())
                           .allowedSubgroup(r.allowedSubgroup())
                           .build());
        }
        return result;
    }

    private void validateSubgroupBelongsToGroup(Subgroup subgroup, Group group) {
        if (subgroup != null && !Objects.equals(subgroup.getGroup().getId(), group.getId())) {
            throw new IllegalArgumentException("Subgroup does not belong to the specified group");
        }
    }

    private List<Lesson> generateSchedule(
        Subject subject,
        Boolean allGroups,
        List<ResolvedScope> scopes,
        BulkScheduleRequest.Entry entry
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
                    lessons.add(lesson(subject, allGroups, scopes, entry.type(), lessonDate));
                }
            }

            weekStart = weekStart.plusWeeks(1);
            weekIndex++;
        }

        return lessons;
    }

    private Lesson lesson(
        Subject subject,
        Boolean allGroups,
        List<ResolvedScope> scopes,
        LessonType type,
        LocalDate startedAt
    ) {
        var lesson = Lesson.builder()
            .subject(subject)
            .type(type)
            .startedAt(startedAt)
            .allGroups(Boolean.TRUE.equals(allGroups))
            .build();
        for (var s : scopes) {
            lesson.getScopes()
                .add(LessonScope.builder()
                         .lesson(lesson)
                         .group(s.group())
                         .allowedSubgroup(s.allowedSubgroup())
                         .build());
        }
        return lesson;
    }

    private record ResolvedScope(
        Group group,

        Subgroup allowedSubgroup
    ) {}
}
