package com.github.k1mb1.vkr_backend.attendance.checkin.domain;

import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionState;
import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Hidden
@Entity
@Table(
        name = "check_in_sessions",
        indexes = {
            @Index(name = "idx_check_in_sessions_lesson_scope", columnList = "lesson_scope_id"),
        })
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckInSessionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_scope_id", nullable = false)
    LessonScopeEntity lessonScope;

    @Column(name = "started_at", nullable = false, columnDefinition = "timestamptz")
    Instant startedAt;

    /**
     * Код аудитории: преподаватель показывает его в аудитории, студент вводит при отметке.
     * Отдаётся только на преподавательской стороне, в публичный ответ не попадает.
     */
    @Column(name = "code", nullable = false, length = 8)
    String code;

    @Column(name = "on_time_seconds", nullable = false)
    int onTimeSeconds;

    @Column(name = "late_seconds", nullable = false)
    int lateSeconds;

    @Column(name = "confirmed_at", columnDefinition = "timestamptz")
    Instant confirmedAt;

    @Column(name = "cancelled_at", columnDefinition = "timestamptz")
    Instant cancelledAt;

    public Instant onTimeEndsAt() {
        return startedAt.plusSeconds(onTimeSeconds);
    }

    public Instant lateEndsAt() {
        return startedAt.plusSeconds((long) onTimeSeconds + lateSeconds);
    }

    public CheckInSessionState stateAt(Instant now) {
        if (cancelledAt != null) {
            return CheckInSessionState.CANCELLED;
        }
        if (confirmedAt != null) {
            return CheckInSessionState.CONFIRMED;
        }
        if (now.isBefore(onTimeEndsAt())) {
            return CheckInSessionState.OPEN;
        }
        if (now.isBefore(lateEndsAt())) {
            return CheckInSessionState.LATE_WINDOW;
        }
        return CheckInSessionState.AWAITING_CONFIRMATION;
    }

    public CheckInRecordStatus statusForCheckInAt(Instant now) {
        if (now.isBefore(onTimeEndsAt())) {
            return CheckInRecordStatus.PRESENT;
        }
        if (now.isBefore(lateEndsAt())) {
            return CheckInRecordStatus.LATE;
        }
        return null;
    }

    public Duration remainingTotal(Instant now) {
        var end = lateEndsAt();
        return now.isBefore(end) ? Duration.between(now, end) : Duration.ZERO;
    }
}
