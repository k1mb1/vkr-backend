package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.*;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import jakarta.persistence.EntityNotFoundException;import java.time.LocalDate;
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
    final StudentGroupRepository studentGroupRepository;
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

    /**
     * Bulk-schedule lessons from a recurring timetable.
     *
     * <p>Algorithm per {@link LessonScheduleEntry}:
     * <ol>
     *   <li><b>WEEKLY</b> – walks weeks starting from the Monday of {@code startDate}'s week,
     *       tracking the repeating {@code intervalWeeks} cycle. For each week it collects all
     *       candidate (date, slot) pairs whose {@code weekIndex} matches and whose
     *       {@code dayOfWeek} does not fall in {@code excludeDates}, <b>sorts them by date</b>
     *       ascending, then creates lessons in that order until {@code totalCount} is reached.</li>
     *   <li><b>ONCE</b> – iterates each slot exactly once using {@code startDate}'s calendar date
     *       adjusted to the slot's {@code dayOfWeek} within the same week. No cycling.
     *       {@code totalCount} still acts as a hard cap.</li>
     *   <li>Duplicate guard – if a lesson with the same (subject, dateTime, type, group) already
     *       exists in the database it is silently skipped (does NOT consume from {@code totalCount}).
     *       This makes repeated calls to bulk-schedule idempotent.</li>
     * </ol>
     */
    @Transactional
    public List<LessonResponse> bulkSchedule(BulkScheduleLessonsRequest request) {
        var subject = subjectService.getReferenceById(request.subjectId());
        var entities = new ArrayList<LessonEntity>();

        for (var entry : request.schedules()) {
            if (entry.recurrence() == RecurrenceType.ONCE) {
                scheduleOnce(entry, subject, entities);
            } else {
                scheduleWeekly(entry, subject, entities);
            }
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

    // -------------------------------------------------------------------------
    // Scheduling strategies
    // -------------------------------------------------------------------------

    /**
     * WEEKLY strategy.
     *
     * <p>Walks week by week. For each week it collects all (date, slot) candidates
     * whose {@code weekIndex} matches the current cycle position, filters out
     * excluded dates, <b>sorts by date ascending</b> so that the generated lessons
     * are always in chronological order regardless of slot ordering in the request,
     * and creates lessons until {@code totalCount} is reached.
     */
    private void scheduleWeekly(
        LessonScheduleEntry entry,
        com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity subject,
        List<LessonEntity> result
    ) {
        int remaining = entry.totalCount();
        int intervalWeeks = entry.intervalWeeks() != null ? entry.intervalWeeks() : 1;
        var excludeSet = new java.util.HashSet<>(entry.resolvedExcludeDates());

        LocalDate weekStart = entry.startDate()
            .toLocalDate()
            .with(java.time.DayOfWeek.MONDAY);
        int cycleWeek = 0;

        while (remaining > 0) {
            // Collect all (date, slot) candidates for this week, sorted by date.
            record Candidate(LocalDate date, LessonSlot slot) {}
            var candidates = entry.slots().stream()
                .filter(s -> s.resolvedWeekIndex() == cycleWeek)
                .map(s -> new Candidate(weekStart.with(s.dayOfWeek()), s))
                .filter(c -> !excludeSet.contains(c.date()))
                .sorted(java.util.Comparator.comparing(Candidate::date)
                    .thenComparing(c -> c.slot().time()))
                .toList();

            for (var candidate : candidates) {
                if (remaining == 0) break;
                var entity = buildLesson(candidate.date(), candidate.slot(), subject);
                if (entity != null) {
                    result.add(entity);
                    remaining--;
                }
            }

            weekStart = weekStart.plusWeeks(1);
            cycleWeek = (cycleWeek + 1) % intervalWeeks;
        }
    }

    /**
     * ONCE strategy.
     *
     * <p>Each slot is placed on its {@code dayOfWeek} within the week of
     * {@code startDate}. Slots are sorted by date+time before creation.
     * {@code totalCount} acts as a hard cap.
     */
    private void scheduleOnce(
        LessonScheduleEntry entry,
        com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity subject,
        List<LessonEntity> result
    ) {
        int remaining = entry.totalCount();
        var excludeSet = new java.util.HashSet<>(entry.resolvedExcludeDates());
        LocalDate weekStart = entry.startDate()
            .toLocalDate()
            .with(java.time.DayOfWeek.MONDAY);

        record Candidate(LocalDate date, LessonSlot slot) {}
        var candidates = entry.slots().stream()
            .map(s -> new Candidate(weekStart.with(s.dayOfWeek()), s))
            .filter(c -> !excludeSet.contains(c.date()))
            .sorted(java.util.Comparator.comparing(Candidate::date)
                .thenComparing(c -> c.slot().time()))
            .toList();

        for (var candidate : candidates) {
            if (remaining == 0) break;
            var entity = buildLesson(candidate.date(), candidate.slot(), subject);
            if (entity != null) {
                result.add(entity);
                remaining--;
            }
        }
    }

    /**
     * Creates a {@link LessonEntity} for the given date+slot, or returns
     * {@code null} if an identical lesson already exists (duplicate guard).
     */
    private LessonEntity buildLesson(
        LocalDate date,
        LessonSlot slot,
        com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity subject
    ) {
        var group = resolveGroup(slot);
        OffsetDateTime dateTime = date.atTime(slot.time()).atOffset(ZoneOffset.UTC);
        UUID groupId = group != null ? group.getId() : null;

        // Idempotency: skip if an identical lesson already exists.
        boolean duplicate = lessonRepository
            .existsBySubject_IdAndDateTimeAndTypeAndGroup_Id(
                subject.getId(), dateTime, slot.type(), groupId
            );
        if (duplicate) return null;

        return LessonEntity.builder()
            .name(buildLessonName(subject.getName(), slot, group))
            .dateTime(dateTime)
            .type(slot.type())
            .subject(subject)
            .group(group)
            .build();
    }

    // -------------------------------------------------------------------------
    // Generic helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the target group entity for a slot, or {@code null} for lectures.
     * Throws {@link EntityNotFoundException} if a non-null groupId does not exist.
     */
    private com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity resolveGroup(
        LessonSlot slot
    ) {
        if (slot.type() == LessonType.LECTURE) {
            // Lectures are always for the whole enrolled cohort.
            return null;
        }
        if (slot.groupId() == null) {
            return null;
        }
        return studentGroupRepository
            .findById(slot.groupId())
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "StudentGroup not found: " + slot.groupId()
                )
            );
    }

    /**
     * Builds a human-readable lesson name, e.g. "Математика — Лекция" or
     * "Математика — Практика (ИСТ-21/1)".
     */
    private String buildLessonName(
        String subjectName,
        LessonSlot slot,
        com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity group
    ) {
        String typeName = switch (slot.type()) {
            case LECTURE  -> "Лекция";
            case PRACTICE -> "Практика";
            case NONE     -> "Занятие";
        };
        if (group != null) {
            return subjectName + " — " + typeName + " (" + group.getName() + ")";
        }
        return subjectName + " — " + typeName;
    }
}
