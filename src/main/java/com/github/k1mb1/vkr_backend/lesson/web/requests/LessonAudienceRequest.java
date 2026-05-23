package com.github.k1mb1.vkr_backend.lesson.web.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;

import java.util.UUID;

@Schema(
    description = "Аудитория проведения занятия без даты: группа (опц. подгруппа) или все группы"
)
public record LessonAudienceRequest(
    @Schema(description = "ID группы. null допустим только если allGroups=true")
    UUID groupId,

    @Schema(description = "ID разрешённой подгруппы (null = вся группа)")
    UUID allowedSubgroupId,

    @Schema(description = "true = проведение для всех групп предмета сразу (groupId должен быть null)")
    boolean allGroups
) {
    @Schema(hidden = true)
    @AssertTrue(message = "groupId is required when allGroups=false")
    public boolean hasGroupOrAllGroups() {
        return allGroups || groupId != null;
    }

    @Schema(hidden = true)
    @AssertTrue(message = "groupId must be null when allGroups=true")
    public boolean noGroupWhenAllGroups() {
        return !allGroups || groupId == null;
    }
}
