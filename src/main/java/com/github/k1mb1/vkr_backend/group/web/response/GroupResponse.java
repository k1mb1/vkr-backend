package com.github.k1mb1.vkr_backend.group.web.response;

import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "Group", description = "Полная информация о группе")
public record GroupResponse(
    @Schema(
        description = "ID группы",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    UUID id,
    @Schema(description = "Название группы", example = "ИС-101") String name,
    @Schema(description = "Список подгрупп") List<SubgroupResponse> subgroups,
    @Schema(description = "Список студентов группы")
    List<StudentResponse> students,
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00Z")
    Instant createdAt,
    @Schema(
        description = "Дата последнего обновления",
        example = "2024-01-02T12:00:00Z"
    )
    Instant updatedAt
) {}
