package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Политика временных окон check-in для всего предмета (опциональная фича).
 * <p>
 * Если {@code enabled = true}, то любая check-in сессия предмета использует именно эти окна
 * ({@code onTimeSeconds} / {@code lateSeconds}), а значения из запроса на запуск игнорируются —
 * так преподаватель задаёт единое время на отметку для всех занятий предмета. Если выключена —
 * окна задаются индивидуально при запуске сессии.
 */
@Hidden
@Entity
@Table(name = "checkin_policies")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckInPolicy extends BaseEntity {

    /** Включена ли единая политика окон check-in для предмета. */
    @Builder.Default
    @Column(nullable = false)
    boolean enabled = false;

    /** Длительность основного окна (секунды); значимо только при enabled=true. */
    @Column
    Integer onTimeSeconds;

    /** Дополнительное окно для опоздавших (секунды); значимо только при enabled=true. */
    @Column
    Integer lateSeconds;
}
