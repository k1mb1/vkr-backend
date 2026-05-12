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
import com.github.k1mb1.vkr_backend.subject.SubjectOfferingReferenceService;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectOffering;
import com.github.k1mb1.vkr_backend.teacher.TeacherReferenceService;
import jakarta.persistence.EntityNotFoundException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LessonService implements LessonApi {

    final LessonRepository lessonRepository;
    final LessonMapper lessonMapper;
    final GroupReferenceService groupReferenceService;
    final TeacherReferenceService teacherReferenceService;
    final SubjectOfferingReferenceService subjectOfferingReferenceService;

    @Transactional
    @Override
    public LessonResponse updateLesson(UUID id, UpdateLessonRequest request) {
        var lesson = lessonRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));

        lessonMapper.updateEntity(request, lesson);

        if (request.teacherId() != null) {
            lesson.setTeacher(teacherReferenceService.getTeacherReferenceById(request.teacherId()));
        }
        if (request.subgroupId() != null) {
            lesson.setSubgroup(groupReferenceService.getSubgroupReferenceById(request.subgroupId()));
        }

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    @Override
    public void deleteLesson(UUID id) {
        var lesson = lessonRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
        lessonRepository.delete(lesson);
    }

    @Override
    public Page<LessonResponse> getLessonPage(LessonFilter filter, Pageable pageable) {
        return lessonRepository
            .findAll(new LessonSpecifications(filter).toSpec(), pageable)
            .map(lessonMapper::toResponse);
    }

    @Transactional
    @Override
    public List<LessonResponse> bulkScheduleLessons(BulkScheduleRequest request) {
        var offerings = requireOfferings(request.subjectId());
        var lessons = new ArrayList<Lesson>();

        for (var offering : offerings) {
            for (var entry : request.schedules()) {
                lessons.addAll(generateSchedule(offering, entry));
            }
        }

        return lessonRepository.saveAll(lessons)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    @Transactional
    @Override
    public List<LessonResponse> createLessonsByType(CreateLessonsByTypeRequest request) {
        var offerings = requireOfferings(request.subjectId());
        var now = Instant.now();
        var lessons = new ArrayList<Lesson>();

        for (var offering : offerings) {
            for (int i = 0; i < request.lectureCount(); i++) {
                lessons.add(lesson(offering, LessonType.LECTURE, now));
            }
            for (int i = 0; i < request.practiceCount(); i++) {
                lessons.add(lesson(offering, LessonType.PRACTICE, now));
            }
        }

        return lessonRepository.saveAll(lessons)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    private List<SubjectOffering> requireOfferings(UUID subjectId) {
        var offerings = subjectOfferingReferenceService.findBySubjectId(subjectId);
        if (offerings.isEmpty()) {
            throw new EntityNotFoundException("No offerings found for subject: " + subjectId);
        }
        return offerings;
    }

    private List<Lesson> generateSchedule(SubjectOffering offering, BulkScheduleRequest.Entry entry) {
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
                if (lessons.size() >= entry.totalCount()) break;
                var lessonDate = weekStart.plusDays(dow.getValue() - DayOfWeek.MONDAY.getValue());
                if (!lessonDate.isBefore(entry.startDate())) {
                    lessons.add(lesson(offering, entry.type(), lessonDate.atStartOfDay(ZoneOffset.UTC).toInstant()));
                }
            }

            weekStart = weekStart.plusWeeks(1);
            weekIndex++;
        }

        return lessons;
    }

    private Lesson lesson(SubjectOffering offering, LessonType type, Instant startedAt) {
        return Lesson.builder()
            .offering(offering)
            .type(type)
            .startedAt(startedAt)
            .build();
    }
}
