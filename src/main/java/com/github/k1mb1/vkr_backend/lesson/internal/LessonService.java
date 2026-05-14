package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.lesson.LessonApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonType;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.UpdateLessonRequest;
import com.github.k1mb1.vkr_backend.lesson.web.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LessonService
    implements LessonApi {

    final LessonRepository lessonRepository;

    final LessonMapper lessonMapper;

    final GroupReferenceService groupReferenceService;

    final TeacherReferenceService teacherReferenceService;

    final SubjectRepository subjectRepository;

    @Transactional
    @Override
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest request) {
        var lesson = lessonRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));

        lessonMapper.updateEntity(request, lesson);

        if (request.subjectId() != null) {
            lesson.setSubject(subjectRepository.getReferenceById(request.subjectId()));
        }
        if (request.groupId() != null) {
            lesson.setGroup(groupReferenceService.getGroupReferenceById(request.groupId()));
        }
        if (request.teacherId() != null) {
            lesson.setTeacher(teacherReferenceService.getTeacherReferenceById(request.teacherId()));
        }
        if (request.subgroupId() != null) {
            var subgroup = groupReferenceService.getSubgroupReferenceById(request.subgroupId());
            var targetGroup = lesson.getGroup();
            if (!subgroup.getGroup().getId().equals(targetGroup.getId())) {
                throw new IllegalArgumentException("Subgroup does not belong to the lesson's group");
            }
            lesson.setSubgroup(subgroup);
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
    public Page<LessonResponse> getLessonPage(LessonFilter filter, Pageable pageable) {
        return lessonRepository.findAll(
                new LessonSpecifications(filter).toSpecification(),
                pageable
            )
            .map(lessonMapper::toResponse);
    }

    @Transactional
    @Override
    public List<LessonResponse> bulkScheduleLessons(
        BulkScheduleRequest request
    ) {
        var subject = subjectRepository.findById(request.subjectId())
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + request.subjectId()));
        var group = groupReferenceService.getGroupReferenceById(request.groupId());

        var lessons = new ArrayList<Lesson>();

        for (var entry : request.schedules()) {
            lessons.addAll(generateSchedule(subject, group, entry));
        }

        return lessonRepository.saveAll(lessons).stream().map(lessonMapper::toResponse).toList();
    }

    @Transactional
    @Override
    public List<LessonResponse> createLessonsByType(
        CreateLessonsByTypeRequest request
    ) {
        var subject = subjectRepository.findById(request.subjectId())
            .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + request.subjectId()));
        var group = groupReferenceService.getGroupReferenceById(request.groupId());
        var now = Instant.now();
        var lessons = new ArrayList<Lesson>();

        for (int i = 0; i < request.lectureCount(); i++) {
            lessons.add(lesson(subject, group, LessonType.LECTURE, now));
        }
        for (int i = 0; i < request.practiceCount(); i++) {
            lessons.add(lesson(subject, group, LessonType.PRACTICE, now));
        }

        return lessonRepository.saveAll(lessons).stream().map(lessonMapper::toResponse).toList();
    }

    private List<Lesson> generateSchedule(
        com.github.k1mb1.vkr_backend.subject.domain.Subject subject,
        com.github.k1mb1.vkr_backend.group.domain.Group group,
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
                    lessons.add(lesson(
                        subject,
                        group,
                        entry.type(),
                        lessonDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                    ));
                }
            }

            weekStart = weekStart.plusWeeks(1);
            weekIndex++;
        }

        return lessons;
    }

    private Lesson lesson(
        com.github.k1mb1.vkr_backend.subject.domain.Subject subject,
        com.github.k1mb1.vkr_backend.group.domain.Group group,
        LessonType type,
        Instant startedAt
    ) {
        return Lesson.builder()
            .subject(subject)
            .group(group)
            .type(type)
            .startedAt(startedAt)
            .build();
    }
}
