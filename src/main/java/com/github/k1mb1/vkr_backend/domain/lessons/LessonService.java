package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.*;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    final LessonRepository lessonRepository;
    final SubjectService subjectService;
    final LessonMapper lessonMapper;

    public List<LessonResponse> findAllBySubjectId(UUID subjectId) {
        return lessonRepository
            .findAllBySubject_Id(subjectId)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    @Transactional
    public LessonResponse create(CreateLessonRequest request) {
        var subject = subjectService.getReferenceById(request.subjectId());
        var entity = lessonMapper
            .toEntity(request)
            .toBuilder()
            .subject(subject)
            .build();
        return lessonMapper.toResponse(lessonRepository.save(entity));
    }

    @Transactional
    public List<LessonResponse> createByType(CreateLessonsByTypeRequest request) {
        var subject = subjectService.getReferenceById(request.subjectId());
        var entities = new ArrayList<LessonEntity>();
        var baseDateTime = OffsetDateTime.now(ZoneOffset.UTC);

        for (int i = 0; i < request.lectureCount(); i++) {
            entities.add(LessonEntity.builder()
                .name(buildLessonName(subject.getName(), LessonType.LECTURE))
                .dateTime(baseDateTime.plusMinutes(i))
                .type(LessonType.LECTURE)
                .subject(subject)
                .build());
        }

        for (int i = 0; i < request.practiceCount(); i++) {
            entities.add(LessonEntity.builder()
                .name(buildLessonName(subject.getName(), LessonType.PRACTICE))
                .dateTime(baseDateTime.plusMinutes(request.lectureCount() + i))
                .type(LessonType.PRACTICE)
                .subject(subject)
                .build());
        }

        return lessonRepository.saveAll(entities)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    /**
     * Bulk lesson creation from a repeating week pattern.
     */
    @Transactional
    public List<LessonResponse> bulkSchedule(BulkScheduleRequest request) {
        var subject = subjectService.getReferenceById(request.subjectId());
        var entities = new ArrayList<LessonEntity>();

        for (var entry : request.schedules()) {
            createByPattern(entry, subject, entities);
        }

        return lessonRepository.saveAll(entities)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    @Transactional
    public void delete(UUID id) {
        lessonRepository.deleteById(id);
    }

    /**
     * Updates the decay factor of a lesson.
     *
     * <p>The front-end multiplies the weighted sum of all task scores for the
     * lesson by this value. 1.0 = no decay; 0.5 = all scores halved.
     */
    @Transactional
    public LessonResponse updateDecayFactor(UUID id, UpdateDecayFactorRequest request) {
        var lesson = lessonRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
        lesson.setDecayFactor(request.decayFactor());
        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    private void createByPattern(
        BulkScheduleRequest.Entry entry,
        SubjectEntity subject,
        List<LessonEntity> result
    ) {
        if (entry.daysOfWeek().stream().allMatch(List::isEmpty)) {
            throw new IllegalArgumentException("daysOfWeek must contain at least one day");
        }

        int remaining = entry.totalCount();
        int cycleWeeks = entry.daysOfWeek().size();
        LocalDate weekStart = entry.startDate()
            .minusDays(entry.startDate().getDayOfWeek().getValue() - 1L);
        int weekOffset = 0;

        while (remaining > 0) {
            var weekPattern = entry.daysOfWeek().get(weekOffset % cycleWeeks)
                .stream()
                .distinct()
                .sorted()
                .toList();

            LocalDate currentWeekStart = weekStart.plusWeeks(weekOffset);
            for (var day : weekPattern) {
                if (remaining == 0) break;

                LocalDate lessonDate = currentWeekStart.plusDays(day.getValue() - 1L);
                if (lessonDate.isBefore(entry.startDate())) {
                    continue;
                }

                result.add(LessonEntity.builder()
                    .name(buildLessonName(subject.getName(), entry.type()))
                    .dateTime(lessonDate.atStartOfDay().atOffset(ZoneOffset.UTC))
                    .type(entry.type())
                    .subject(subject)
                    .build());
                remaining--;
            }
            weekOffset++;
        }
    }

    /**
     * Builds a human-readable lesson name, e.g. "Математика — Лекция" or
     * "Математика — Практика (ИСТ-21/1)".
     */
    private String buildLessonName(
        String subjectName,
        LessonType type
    ) {
        String typeName = switch (type) {
            case LECTURE  -> "Лекция";
            case PRACTICE -> "Практика";
            case NONE     -> "Занятие";
        };
        return subjectName + " — " + typeName;
    }
}
