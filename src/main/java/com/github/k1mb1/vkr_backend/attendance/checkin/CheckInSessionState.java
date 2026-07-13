package com.github.k1mb1.vkr_backend.attendance.checkin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Производный статус check-in сессии")
public enum CheckInSessionState {
    /**
     * Идёт основное окно — отметки PRESENT.
     */
    OPEN,

    /**
     * Идёт окно для опоздавших — отметки LATE.
     */
    LATE_WINDOW,

    /**
     * Окно закрылось, ждёт сверки преподавателем.
     */
    AWAITING_CONFIRMATION,

    /**
     * Сессия сверена, данные перенесены в основную посещаемость.
     */
    CONFIRMED,

    /**
     * Сессия отменена преподавателем.
     */
    CANCELLED,
}
