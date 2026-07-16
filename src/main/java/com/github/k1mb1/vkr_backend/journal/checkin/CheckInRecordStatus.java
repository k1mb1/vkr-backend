package com.github.k1mb1.vkr_backend.journal.checkin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус check-in записи студента")
public enum CheckInRecordStatus {
    /**
     * Студент отметился в основном окне.
     */
    PRESENT,

    /**
     * Студент отметился в окне для опоздавших.
     */
    LATE,
}
