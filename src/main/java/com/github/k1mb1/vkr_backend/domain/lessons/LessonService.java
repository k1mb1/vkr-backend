package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.LessonScheduleEntry;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.LessonSlot;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.RecurrenceType;
import com.github.k1mb1.vkr_backend.domain.lessons.responses.LessonResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import jakarta.persistence.EntityNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
        var subject = subjectService.findEntityById(request.subjectId());
        var entity = lessonMapper
            .toEntity(request)
            .toBuilder()
            .subject(subject)
            .build();
        return lessonMapper.toResponse(lessonRepository.save(entity));
    }

    @Transactional
    public List<LessonResponse> createByType(
        CreateLessonsByTypeRequest request
    ) {
        var subject = subjectService.findEntityById(request.subjectId());

        var lectures = IntStream.range(0, request.lectureCount()).mapToObj(i ->
            LessonEntity.builder()
                .name("Лекция " + (i + 1))
                .type(LessonType.LECTURE)
                .subject(subject)
                .build()
        );

        var practices = IntStream.range(0, request.practiceCount()).mapToObj(
            i ->
                LessonEntity.builder()
                    .name("Практика " + (i + 1))
                    .type(LessonType.PRACTICE)
                    .subject(subject)
                    .build()
        );

        return Stream.concat(lectures, practices)
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    lessonRepository::saveAll
                )
            )
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    @Transactional
    public LessonResponse update(UUID id, UpdateLessonRequest request) {
        var entity = lessonRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Lesson not found: " + id)
            );
        if (request.subjectId() != null) {
            var subject = subjectService.findEntityById(request.subjectId());
            entity.toBuilder().subject(subject).build();
        }
        lessonMapper.update(entity, request);
        return lessonMapper.toResponse(lessonRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!lessonRepository.existsById(id)) {
            throw new EntityNotFoundException("Lesson not found: " + id);
        }
        lessonRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Bulk schedule creation
    // -------------------------------------------------------------------------

    /**
     * Generates lessons for each {@link LessonScheduleEntry} in the request and
     * saves them all in a single batch.
     *
     * <p>When two entries produce lessons on the same date AND have the same type,
     * the generated names receive an ordinal suffix so they stay distinguishable,
     * e.g. "Лекция 3 (2)" for the second lecture on that date.
     */
    @Transactional
    public List<LessonResponse> bulkSchedule(BulkScheduleLessonsRequest request) {
        var subject = subjectService.findEntityById(request.subjectId());

        // (date, type, dateTime) tuple — one per generated lesson
        record Slot(LocalDate date, LessonType type, OffsetDateTime dateTime) {}

        List<Slot> slots = new ArrayList<>();
        for (LessonScheduleEntry entry : request.schedules()) {
            // expandSlots returns (date, lessonSlot) pairs in chronological order
            List<Map.Entry<LocalDate, LessonSlot>> expanded = expandSlots(entry);
            for (var pair : expanded) {
                LocalDate d = pair.getKey();
                LessonSlot ls = pair.getValue();
                OffsetDateTime odt = OffsetDateTime.of(d, ls.time(), ZoneOffset.UTC);
                slots.add(new Slot(d, ls.type(), odt));
            }
        }

        // Sort chronologically so ordinal numbering is stable
        slots.sort(Comparator.comparing(Slot::dateTime));

        // Count how many times each (date, type) combo appears — detect same-day collisions
        Map<String, Integer> dayTypeCount = new HashMap<>();
        Map<String, Integer> dayTypeSeen  = new HashMap<>();
        for (Slot s : slots) {
            dayTypeCount.merge(s.date() + "|" + s.type(), 1, Integer::sum);
        }

        // Global per-type ordinal counter (Лекция 1, Лекция 2 …)
        Map<LessonType, Integer> typeCounter = new HashMap<>();

        List<LessonEntity> entities = new ArrayList<>();
        for (Slot s : slots) {
            int globalOrdinal = typeCounter.merge(s.type(), 1, Integer::sum);
            String key = s.date() + "|" + s.type();
            int dailyOrdinal = dayTypeSeen.merge(key, 1, Integer::sum);
            boolean collision = dayTypeCount.get(key) > 1;

            String name = collision
                ? labelPrefix(s.type()) + " " + globalOrdinal + " (" + dailyOrdinal + ")"
                : labelPrefix(s.type()) + " " + globalOrdinal;

            entities.add(
                LessonEntity.builder()
                    .name(name)
                    .type(s.type())
                    .dateTime(s.dateTime())
                    .subject(subject)
                    .build()
            );
        }

        return lessonRepository.saveAll(entities)
            .stream()
            .map(lessonMapper::toResponse)
            .toList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static String labelPrefix(LessonType type) {
        return switch (type) {
            case LECTURE  -> "Лекция";
            case PRACTICE -> "Практика";
            default       -> "Занятие";
        };
    }

    /**
     * Expands a {@link LessonScheduleEntry} into an ordered list of (date, slot) pairs,
     * respecting {@code totalCount} as the combined stop condition across all slots.
     */
    private static List<Map.Entry<LocalDate, LessonSlot>> expandSlots(LessonScheduleEntry entry) {
        return switch (entry.recurrence()) {
            case WEEKLY  -> expandWeekly(entry);
            case MONTHLY -> expandMonthly(entry);
        };
    }

    /**
     * Walks the interval cycle week by week.
     * Within each cycle of {@code intervalWeeks} weeks, only slots whose
     * {@code weekIndex} matches the current cycle-offset week fire.
     *
     * <p>Days within one week are emitted in DayOfWeek order as declared in the slot.
     * All slots are interleaved chronologically before the totalCount cap is applied.
     */
    private static List<Map.Entry<LocalDate, LessonSlot>> expandWeekly(LessonScheduleEntry entry) {
        List<Map.Entry<LocalDate, LessonSlot>> result = new ArrayList<>();
        int interval = entry.intervalWeeks();

        // Anchor to Monday of the week containing startDate — cycle offset 0
        LocalDate cycleStart = entry.startDate().with(DayOfWeek.MONDAY);

        outer:
        while (true) {
            // Iterate over each week within this cycle
            for (int weekOffset = 0; weekOffset < interval; weekOffset++) {
                LocalDate weekStart = cycleStart.plusWeeks(weekOffset);

                // Fire every slot whose weekIndex matches this week
                for (LessonSlot slot : entry.slots()) {
                    if (slot.resolvedWeekIndex() != weekOffset) continue;

                    for (DayOfWeek dow : slot.daysOfWeek()) {
                        LocalDate candidate = weekStart.with(dow);
                        if (candidate.isBefore(entry.startDate())) continue;
                        result.add(Map.entry(candidate, slot));
                        if (result.size() >= entry.totalCount()) break outer;
                    }
                }
            }
            cycleStart = cycleStart.plusWeeks(interval);
        }

        return result;
    }

    /**
     * Monthly expansion: for each month, fires every slot once by finding the first
     * occurrence of each listed weekday on or after the anchor day of that month.
     * {@code weekIndex} is ignored for monthly recurrence.
     */
    private static List<Map.Entry<LocalDate, LessonSlot>> expandMonthly(LessonScheduleEntry entry) {
        List<Map.Entry<LocalDate, LessonSlot>> result = new ArrayList<>();
        int interval = entry.intervalMonths();
        int anchorDayOfMonth = entry.startDate().getDayOfMonth();

        LocalDate monthCursor = entry.startDate().withDayOfMonth(1);

        outer:
        while (true) {
            LocalDate anchor = monthCursor.withDayOfMonth(
                Math.min(anchorDayOfMonth, monthCursor.lengthOfMonth())
            );

            for (LessonSlot slot : entry.slots()) {
                for (DayOfWeek dow : slot.daysOfWeek()) {
                    LocalDate candidate = anchor;
                    while (candidate.getDayOfWeek() != dow) {
                        candidate = candidate.plusDays(1);
                    }
                    if (candidate.isBefore(entry.startDate())) continue;

                    result.add(Map.entry(candidate, slot));
                    if (result.size() >= entry.totalCount()) break outer;
                }
            }

            monthCursor = monthCursor.plusMonths(interval);
        }

        return result;
    }
}
