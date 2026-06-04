package com.github.k1mb1.vkr_backend.subject.domain;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Политика временных окон check-in для всего предмета (опциональная фича).
 * <p>
 * Если {@code enabled = true}, то любая check-in сессия предмета использует именно эти окна
 * ({@code onTimeSeconds} / {@code lateSeconds}), а значения из запроса на запуск игнорируются —
 * так преподаватель задаёт единое время на отметку для всех занятий предмета. Если выключена —
 * окна задаются индивидуально при запуске сессии.
 */
@Hidden
@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckInPolicy {

    /** Включена ли единая политика окон check-in для предмета. */
    @Builder.Default
    @Column(name = "checkin_enabled", nullable = false)
    boolean enabled = false;

    /** Длительность основного окна (секунды); значимо только при enabled=true. */
    @Column(name = "checkin_on_time_seconds")
    Integer onTimeSeconds;

    /** Дополнительное окно для опоздавших (секунды); значимо только при enabled=true. */
    @Column(name = "checkin_late_seconds")
    Integer lateSeconds;
}
