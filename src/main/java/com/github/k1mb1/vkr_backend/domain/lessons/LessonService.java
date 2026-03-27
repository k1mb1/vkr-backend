package com.github.k1mb1.vkr_backend.domain.lessons;

import com.github.k1mb1.vkr_backend.domain.lessons.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.CreateLessonsByTypeRequest;
import com.github.k1mb1.vkr_backend.domain.lessons.requests.LessonScheduleEntry;
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

        // 1. Build (date, type, time, labelBase) tuples from every entry
        record Slot(LocalDate date, LessonType type, OffsetDateTime dateTime, String labelBase) {}

        List<Slot> slots = new ArrayList<>();
        for (LessonScheduleEntry entry : request.schedules()) {
            List<LocalDate> dates = expandDates(entry);
            String labelBase = labelPrefix(entry.type());
            for (LocalDate d : dates) {
                OffsetDateTime odt = OffsetDateTime.of(d, entry.time(), ZoneOffset.UTC);
                slots.add(new Slot(d, entry.type(), odt, labelBase));
            }
        }

        // 2. Sort chronologically so ordinal numbering is predictable
        slots.sort(Comparator.comparing(Slot::dateTime));

        // 3. Assign per-type global ordinals and per-(date,type) daily ordinals
        //    to detect collisions (2 lessons of same type on same day)
        Map<LessonType, Integer> typeCounter = new HashMap<>();
        // key = "date|type", value = count of slots landing on that combo
        Map<String, Integer> dayTypeCount = new HashMap<>();
        Map<String, Integer> dayTypeSeen  = new HashMap<>();

        for (Slot s : slots) {
            String key = s.date() + "|" + s.type();
            dayTypeCount.merge(key, 1, Integer::sum);
        }

        List<LessonEntity> entities = new ArrayList<>();
        for (Slot s : slots) {
            int globalOrdinal = typeCounter.merge(s.type(), 1, Integer::sum);
            String key = s.date() + "|" + s.type();
            int dailyOrdinal = dayTypeSeen.merge(key, 1, Integer::sum);
            boolean collision = dayTypeCount.get(key) > 1;

            String name = collision
                ? s.labelBase() + " " + globalOrdinal + " (" + dailyOrdinal + ")"
                : s.labelBase() + " " + globalOrdinal;

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
     * Expands a single {@link LessonScheduleEntry} into the list of concrete dates
     * on which lessons should be created.
     */
    private static List<LocalDate> expandDates(LessonScheduleEntry entry) {
        return switch (entry.recurrence()) {
            case WEEKLY  -> expandWeekly(entry);
            case MONTHLY -> expandMonthly(entry);
        };
    }

    /**
     * Weekly expansion: starting from {@code startDate}, advance week-by-week in
     * steps of {@code intervalWeeks} and emit a date for each matching day-of-week.
     *
     * <p>The algorithm walks Monday-anchored "week windows" of width {@code intervalWeeks * 7}
     * days. Within every selected window it emits any day that matches {@code daysOfWeek}
     * and falls on or after {@code startDate}.
     */
    private static List<LocalDate> expandWeekly(LessonScheduleEntry entry) {
        List<LocalDate> result = new ArrayList<>();
        int interval = entry.intervalWeeks();

        // Anchor to Monday of the week containing startDate
        LocalDate weekStart = entry.startDate().with(DayOfWeek.MONDAY);

        outer:
        while (true) {
            // Emit matching days within this week window
            for (DayOfWeek dow : entry.daysOfWeek()) {
                LocalDate candidate = weekStart.with(dow);
                if (candidate.isBefore(entry.startDate())) continue;
                result.add(candidate);
                if (result.size() >= entry.totalCount()) break outer;
            }
            weekStart = weekStart.plusWeeks(interval);
        }
        return result;
    }

    /**
     * Monthly expansion: for each selected month (stepping by {@code intervalMonths}),
     * find the first occurrence of each listed day-of-week that falls on or after
     * the day-of-month of {@code startDate}.
     */
    private static List<LocalDate> expandMonthly(LessonScheduleEntry entry) {
        List<LocalDate> result = new ArrayList<>();
        int interval = entry.intervalMonths();
        int anchorDayOfMonth = entry.startDate().getDayOfMonth();

        // Start from the month containing startDate
        LocalDate monthCursor = entry.startDate().withDayOfMonth(1);

        outer:
        while (true) {
            LocalDate anchor = monthCursor.withDayOfMonth(
                Math.min(anchorDayOfMonth, monthCursor.lengthOfMonth())
            );

            for (DayOfWeek dow : entry.daysOfWeek()) {
                // Find first occurrence of this weekday on or after anchor
                LocalDate candidate = anchor;
                while (candidate.getDayOfWeek() != dow) {
                    candidate = candidate.plusDays(1);
                }
                if (candidate.isBefore(entry.startDate())) continue;

                result.add(candidate);
                if (result.size() >= entry.totalCount()) break outer;
            }

            monthCursor = monthCursor.plusMonths(interval);
        }
        return result;
    }
}
