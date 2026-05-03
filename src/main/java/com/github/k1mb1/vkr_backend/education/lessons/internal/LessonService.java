package com.github.k1mb1.vkr_backend.education.lessons.internal;

import com.github.k1mb1.vkr_backend.education.lessons.api.IssuanceMode;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonFilterRequest;
import com.github.k1mb1.vkr_backend.education.lessons.api.requests.*;
import com.github.k1mb1.vkr_backend.education.lessons.api.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.education.structure.api.StructureQueryFacade;
import com.github.k1mb1.vkr_backend.education.subjects.api.SubjectQueryFacade;
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
    final LessonMapper lessonMapper;
    final SubjectQueryFacade subjectQueryFacade;
    final StructureQueryFacade structureQueryFacade;

    public Page<LessonResponse> findAll(LessonFilterRequest filterReq, Pageable pageable) {
        var filter = LessonFilter.builder()
            .subjectId(filterReq.subjectId())
            .lessonType(filterReq.lessonType())
            .groupId(filterReq.groupId())
            .build();
        return lessonRepository.findAll(filter.toSpecification(), pageable)
            .map(e -> toResponseWithSubgroup(e));
    }

    @Transactional
    public LessonResponse create(CreateLessonRequest request) {
        if (!subjectQueryFacade.existsById(request.subjectId()))
            throw new EntityNotFoundException("Subject not found: " + request.subjectId());
        if (request.groupId() != null && !structureQueryFacade.groupExists(request.groupId()))
            throw new EntityNotFoundException("Group not found: " + request.groupId());

        var entity = lessonMapper.toEntity(request);
        return toResponseWithSubgroup(lessonRepository.save(entity));
    }

    @Transactional
    public List<LessonResponse> bulkSchedule(BulkScheduleRequest request) {
        if (!subjectQueryFacade.existsById(request.subjectId()))
            throw new EntityNotFoundException("Subject not found: " + request.subjectId());
        String subjectName = subjectQueryFacade.getNameById(request.subjectId());

        var entities = new ArrayList<LessonEntity>();
        for (var entry : request.schedules()) {
            createByPattern(entry, request.subjectId(), subjectName, entities);
        }

        var unique = entities.stream()
            .filter(e -> !lessonRepository.existsBySubjectIdAndDateTimeAndTypeAndGroupId(
                e.getSubjectId(), e.getDateTime(), e.getType(), e.getGroupId()))
            .toList();

        return lessonRepository.saveAll(unique).stream()
            .map(this::toResponseWithSubgroup)
            .toList();
    }

    @Transactional
    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        var lesson = getById(id);
        lessonMapper.update(lesson, request);
        if (request.groupId() != null) {
            if (!structureQueryFacade.groupExists(request.groupId()))
                throw new EntityNotFoundException("Group not found: " + request.groupId());
            lesson.setGroupId(request.groupId());
        }
        return toResponseWithSubgroup(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponse issueLesson(UUID id) {
        var lesson = getById(id);
        if (lesson.getIssuanceMode() == IssuanceMode.AUTO)
            throw new IllegalStateException("Lesson " + id + " uses AUTO issuance mode.");
        if (lesson.getIssuedAt() == null) {
            lesson.setIssuedAt(Instant.now());
            lessonRepository.save(lesson);
        }
        return toResponseWithSubgroup(lesson);
    }

    @Transactional
    public LessonResponse updateIssuedTaskIndex(UUID id, UpdateIssuedTaskIndexRequest request) {
        var lesson = getById(id);
        lesson.setIssuedTaskIndex(request.issuedTaskIndex());
        return toResponseWithSubgroup(lessonRepository.save(lesson));
    }

    @Transactional
    public void delete(UUID id) {
        lessonRepository.deleteById(id);
    }

    LessonEntity getById(UUID id) {
        return lessonRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + id));
    }

    private LessonResponse toResponseWithSubgroup(LessonEntity entity) {
        Integer subgroupNumber = null;
        if (entity.getGroupId() != null) {
            try {
                String name = structureQueryFacade.getGroupNameById(entity.getGroupId());
                subgroupNumber = extractSubgroupNumber(name);
            } catch (Exception ignored) {}
        }
        var base = lessonMapper.toResponse(entity);
        return new LessonResponse(
            base.id(), base.name(), base.dateTime(), base.type(),
            base.subjectId(), base.groupId(), subgroupNumber,
            base.issuanceMode(), base.issuedAt(), base.issuedTaskIndex(),
            base.penaltyMode(), base.penaltyStep(), base.createdAt(), base.updatedAt()
        );
    }

    private Integer extractSubgroupNumber(String name) {
        if (name == null) return null;
        int slash = name.lastIndexOf('/');
        if (slash < 0 || slash == name.length() - 1) return null;
        try { return Integer.parseInt(name.substring(slash + 1)); }
        catch (NumberFormatException e) { return null; }
    }

    private void createByPattern(BulkScheduleRequest.Entry entry, UUID subjectId, String subjectName, List<LessonEntity> result) {
        if (entry.daysOfWeek().stream().allMatch(List::isEmpty))
            throw new IllegalArgumentException("daysOfWeek must contain at least one day");
        int remaining = entry.totalCount();
        int cycleWeeks = entry.daysOfWeek().size();
        LocalDate weekStart = entry.startDate().minusDays(entry.startDate().getDayOfWeek().getValue() - 1L);
        int weekOffset = 0;
        while (remaining > 0) {
            var weekPattern = entry.daysOfWeek().get(weekOffset % cycleWeeks).stream().distinct().sorted().toList();
            LocalDate currentWeekStart = weekStart.plusWeeks(weekOffset);
            for (var day : weekPattern) {
                if (remaining == 0) break;
                LocalDate lessonDate = currentWeekStart.plusDays(day.getValue() - 1L);
                if (lessonDate.isBefore(entry.startDate())) continue;
                result.add(LessonEntity.builder()
                    .name(subjectName + " — " + switch (entry.type()) {
                        case LECTURE -> "Лекция"; case PRACTICE -> "Практика"; case NONE -> "Занятие";
                    })
                    .dateTime(lessonDate.atStartOfDay().atOffset(ZoneOffset.UTC))
                    .type(entry.type())
                    .subjectId(subjectId)
                    .build());
                remaining--;
            }
            weekOffset++;
        }
    }
}
