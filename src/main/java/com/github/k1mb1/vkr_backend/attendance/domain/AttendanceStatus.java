package com.github.k1mb1.vkr_backend.attendance.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус посещаемости студента на занятии")
public enum AttendanceStatus {
    NONE,
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
