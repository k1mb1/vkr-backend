package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.*;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
public class LessonService {

    final LessonRepository lessonRepository;
    final SubjectService subjectService;
    final StudentGroupRepository groupRepository;
    final LessonMapper lessonMapper;

    public Page<LessonResponse> findAll(LessonFilter filter, Pageable pageable) {
        return lessonRepository
            .findAll(filter.toSpecification(), pageable)
            .map(lessonMapper::toResponse);
    }

    @Transactional
    public LessonResponse create(CreateLessonRequest request) {
        var subject = subjectService.getReferenceById(request.subjectId());
        var builder = lessonMapper
            .toEntity(request)
            .toBuilder()
            .subject(subject);

        if (request.groupId() != null) {
            var group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Group not found: " + request.groupId()
                ));
            builder.group(group);
        }

        return lessonMapper.toResponse(lessonRepository.save(builder.build()));
    }

    /** Bulk lesson creation from a repeating week pattern. */
    @Transactional
    public List<LessonResponse> bulkSchedule(BulkScheduleRequest request) {
        var subject = subjectService.getReferenceById(request.subjectId());
        var entities = new ArrayList<LessonEntity>();

        for (var entry : request.schedules()) {
            createByPattern(entry, subject, entities);
        }

        // Deduplicate against existing lessons
        var unique = entities.stream()
            .filter(e -> !lessonRepository.existsBySubject_IdAndDateTimeAndTypeAndGroup_Id(
                subject.getId(),
                e.getDateTime(),
                e.getType(),
                e.getGroup() != null ? e.getGroup().getId() : null
            ))
            .toList();

        return lessonRepository.saveAll(unique)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    /** Partial update: only non-null fields from the request are applied. */
    @Transactional
    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        var lesson = getById(id);
        lessonMapper.update(lesson, request);

        if (request.groupId() != null) {
            var group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Group not found: " + request.groupId()
                ));
            lesson.setGroup(group);
        }

        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public void delete(UUID id) {
        lessonRepository.deleteById(id);
    }

    /**
     * Manually issues a lesson (only valid for MANUAL issuance mode).
     * Idempotent: if already issued, returns the current state without modification.
     */
    @Transactional
    public LessonResponse issueLesson(UUID id) {
        var lesson = getById(id);
        if (lesson.getIssuanceMode() == IssuanceMode.AUTO) {
            throw new IllegalStateException(
                "Lesson " + id + " uses AUTO issuance mode and cannot be issued manually."
            );
        }
        if (lesson.getIssuedAt() == null) {
            lesson.setIssuedAt(Instant.now());
            lessonRepository.save(lesson);
        }
        return lessonMapper.toResponse(lesson);
    }

    /**
     * Updates the index of the task currently being presented to students.
     * Tasks with position < issuedTaskIndex are considered superseded and
     * receive a displacement penalty on the front-end.
     */
    @Transactional
    public LessonResponse updateIssuedTaskIndex(UUID id, UpdateIssuedTaskIndexRequest request) {
        var lesson = getById(id);
        lesson.setIssuedTaskIndex(request.issuedTaskIndex());
        return lessonMapper.toResponse(lessonRepository.save(lesson));
    }

    private LessonEntity getById(UUID id) {
        return lessonRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
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
                if (lessonDate.isBefore(entry.startDate())) continue;

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

    private String buildLessonName(String subjectName, LessonType type) {
        String typeName = switch (type) {
            case LECTURE  -> "Лекция";
            case PRACTICE -> "Практика";
            case NONE     -> "Занятие";
        };
        return subjectName + " — " + typeName;
    }
}
