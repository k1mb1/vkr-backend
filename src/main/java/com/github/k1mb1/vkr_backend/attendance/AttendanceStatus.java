package com.github.k1mb1.vkr_backend.attendance;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус посещаемости студента на занятии")
public enum AttendanceStatus {
    /**
     * Студент присутствовал.
     */
    PRESENT,

    /**
     * Студент отсутствовал без уважительной причины.
     */
    ABSENT,

    /**
     * Студент присутствовал, но опоздал.
     */
    LATE,

    /**
     * Студент отсутствовал по уважительной причине.
     */
    EXCUSED,
}
