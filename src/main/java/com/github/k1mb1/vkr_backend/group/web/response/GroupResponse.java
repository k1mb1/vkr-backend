package com.github.k1mb1.vkr_backend.group.web.response;

import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Полная информация о группе")
public record GroupResponse(
        @Schema(description = "ID группы") UUID id,

        @Schema(description = "Название группы") String name,

        @Schema(description = "Список подгрупп") List<SubgroupResponse> subgroups,

        @Schema(description = "Список студентов группы") List<StudentResponse> students,

        @Schema(description = "Дата создания") Instant createdAt,

        @Schema(description = "Дата последнего обновления") Instant updatedAt) {}
