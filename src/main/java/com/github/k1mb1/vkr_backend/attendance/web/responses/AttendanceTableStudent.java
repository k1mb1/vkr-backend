package com.github.k1mb1.vkr_backend.attendance.web.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Студент в таблице посещаемости")
public record AttendanceTableStudent(
    @Schema(description = "ID студента") UUID id,

    @Schema(description = "Имя студента") String username,

    @Schema(description = "ID подгруппы (null = без подгруппы)") UUID subgroupId
) {}
