package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.*;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
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
     * <p>For each {@link LessonScheduleEntry} the algorithm:
     * <ol>
     *   <li>Walks from {@code startDate} one week at a time, tracking the current
     *       position inside the repeating {@code intervalWeeks} cycle.</li>
     *   <li>For every day in the current week it checks each slot whose
     *       {@code weekIndex} matches the cycle position and whose
     *       {@code daysOfWeek} includes that day-of-week.</li>
     *   <li>Creates a {@link LessonEntity} with:
     *       <ul>
     *         <li>{@code group = null} when {@code type == LECTURE} (whole cohort)</li>
     *         <li>{@code group = <subgroup>} when {@code type == PRACTICE} and a
     *             {@code groupId} is provided</li>
     *       </ul>
     *   </li>
     *   <li>Stops once {@code totalCount} lessons have been generated for that entry.</li>
     * </ol>
     */
    @Transactional
    public List<LessonResponse> bulkSchedule(BulkScheduleLessonsRequest request) {
        var subject = subjectService.getReferenceById(request.subjectId());
        var entities = new ArrayList<LessonEntity>();

        for (var entry : request.schedules()) {
            int remaining = entry.totalCount();
            int intervalWeeks = entry.intervalWeeks() != null
                ? entry.intervalWeeks()
                : 1;

            // Walk week by week starting from startDate's Monday.
            LocalDate weekStart = entry.startDate()
                .toLocalDate()
                .with(java.time.DayOfWeek.MONDAY);
            int cycleWeek = 0; // 0-based position inside the repeating cycle

            while (remaining > 0) {
                for (var slot : entry.slots()) {
                    if (remaining == 0) break;
                    if (slot.resolvedWeekIndex() != cycleWeek) continue;

                    for (var dow : slot.daysOfWeek()) {
                        if (remaining == 0) break;

                        LocalDate lessonDate = weekStart.with(dow);
                        OffsetDateTime dateTime = lessonDate
                            .atTime(slot.time())
                            .atOffset(ZoneOffset.UTC);

                        // Resolve group reference: null for lectures.
                        var group = resolveGroup(slot);

                        String lessonName = buildLessonName(
                            subject.getName(), slot, group
                        );

                        var lesson = LessonEntity.builder()
                            .name(lessonName)
                            .dateTime(dateTime)
                            .type(slot.type())
                            .subject(subject)
                            .group(group)
                            .build();

                        entities.add(lesson);
                        remaining--;
                    }
                }

                weekStart = weekStart.plusWeeks(1);
                cycleWeek = (cycleWeek + 1) % intervalWeeks;
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

    // -------------------------------------------------------------------------
    // Helpers
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
