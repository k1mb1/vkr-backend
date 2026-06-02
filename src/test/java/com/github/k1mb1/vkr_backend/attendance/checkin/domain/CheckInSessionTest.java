package com.github.k1mb1.vkr_backend.attendance.checkin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit-тесты конечного автомата check-in сессии. Логика чисто временная и не зависит
 * от БД, поэтому проверяется без Spring-контекста и Testcontainers.
 */
class CheckInSessionTest {

    private static final Instant T0 = Instant.parse("2026-06-02T10:00:00Z");

    private static final int ON_TIME_SECONDS = 60;

    private static final int LATE_SECONDS = 30;

    private CheckInSession session() {
        return CheckInSession.builder()
            .startedAt(T0)
            .onTimeSeconds(ON_TIME_SECONDS)
            .lateSeconds(LATE_SECONDS)
            .build();
    }

    @Test
    void computesWindowBoundaries() {
        var session = session();

        assertThat(session.onTimeEndsAt()).isEqualTo(T0.plusSeconds(60));
        assertThat(session.lateEndsAt()).isEqualTo(T0.plusSeconds(90));
    }

    @Nested
    class StateAt {

        @Test
        void openDuringOnTimeWindow() {
            assertThat(session().stateAt(T0.plusSeconds(10)))
                .isEqualTo(CheckInSessionState.OPEN);
        }

        @Test
        void lateWindowAtOnTimeBoundary() {
            // граница on-time не включается в OPEN: now == onTimeEndsAt уже LATE_WINDOW
            assertThat(session().stateAt(T0.plusSeconds(60)))
                .isEqualTo(CheckInSessionState.LATE_WINDOW);
        }

        @Test
        void awaitingConfirmationAtLateBoundary() {
            assertThat(session().stateAt(T0.plusSeconds(90)))
                .isEqualTo(CheckInSessionState.AWAITING_CONFIRMATION);
        }

        @Test
        void confirmedRegardlessOfTime() {
            var session = session();
            session.setConfirmedAt(T0.plusSeconds(120));

            assertThat(session.stateAt(T0.plusSeconds(5)))
                .isEqualTo(CheckInSessionState.CONFIRMED);
        }

        @Test
        void cancelledTakesPriorityOverConfirmed() {
            var session = session();
            session.setConfirmedAt(T0.plusSeconds(120));
            session.setCancelledAt(T0.plusSeconds(130));

            assertThat(session.stateAt(T0.plusSeconds(200)))
                .isEqualTo(CheckInSessionState.CANCELLED);
        }
    }

    @Nested
    class StatusForCheckInAt {

        @Test
        void presentDuringOnTimeWindow() {
            assertThat(session().statusForCheckInAt(T0.plusSeconds(30)))
                .isEqualTo(CheckInRecordStatus.PRESENT);
        }

        @Test
        void lateDuringLateWindow() {
            assertThat(session().statusForCheckInAt(T0.plusSeconds(75)))
                .isEqualTo(CheckInRecordStatus.LATE);
        }

        @Test
        void nullAfterWindowElapsed() {
            assertThat(session().statusForCheckInAt(T0.plusSeconds(90))).isNull();
        }
    }

    @Nested
    class RemainingTotal {

        @Test
        void positiveBeforeEnd() {
            assertThat(session().remainingTotal(T0.plusSeconds(70)))
                .isEqualTo(Duration.ofSeconds(20));
        }

        @Test
        void zeroAtEnd() {
            assertThat(session().remainingTotal(T0.plusSeconds(90)))
                .isEqualTo(Duration.ZERO);
        }

        @Test
        void zeroAfterEnd() {
            assertThat(session().remainingTotal(T0.plusSeconds(200)))
                .isEqualTo(Duration.ZERO);
        }
    }
}
