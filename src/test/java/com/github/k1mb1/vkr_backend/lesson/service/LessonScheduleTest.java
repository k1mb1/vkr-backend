package com.github.k1mb1.vkr_backend.lesson.service;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class LessonScheduleTest {

    // 2026-09-07 — понедельник.
    private static final LocalDate FIRST_MONDAY = LocalDate.of(2026, 9, 7);

    @Test
    void everyTwoWeeksWhenSecondWeekIsEmpty() {
        var dates = LessonService.schedule(FIRST_MONDAY, 5, List.of(List.of(MONDAY, THURSDAY), List.of()));

        assertThat(dates)
                .containsExactly(
                        LocalDate.of(2026, 9, 7), // нед.1 пн
                        LocalDate.of(2026, 9, 10), // нед.1 чт
                        LocalDate.of(2026, 9, 21), // нед.3 пн
                        LocalDate.of(2026, 9, 24), // нед.3 чт
                        LocalDate.of(2026, 10, 5) // нед.5 пн — обрезано по count
                        );
    }

    @Test
    void firstDateAlwaysEqualsFirstLessonDate() {
        var dates = LessonService.schedule(FIRST_MONDAY, 3, List.of(List.of(MONDAY)));

        assertThat(dates).hasSize(3);
        assertThat(dates.getFirst()).isEqualTo(FIRST_MONDAY);
    }

    @Test
    void daysAreOrderedWithinWeekRegardlessOfInputOrder() {
        var dates = LessonService.schedule(FIRST_MONDAY, 3, List.of(List.of(THURSDAY, TUESDAY, MONDAY)));

        assertThat(dates)
                .containsExactly(
                        LocalDate.of(2026, 9, 7), // пн
                        LocalDate.of(2026, 9, 8), // вт
                        LocalDate.of(2026, 9, 10) // чт
                        );
    }

    @Test
    void truncatesExactlyToCountMidWeek() {
        var dates = LessonService.schedule(FIRST_MONDAY, 1, List.of(List.of(MONDAY, THURSDAY)));

        assertThat(dates).containsExactly(LocalDate.of(2026, 9, 7));
    }

    @Test
    void weeklyPatternRepeatsEveryWeek() {
        var dates = LessonService.schedule(FIRST_MONDAY, 3, List.of(List.of(MONDAY)));

        assertThat(dates)
                .containsExactly(LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 21));
    }
}
