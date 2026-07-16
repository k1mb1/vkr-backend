package com.github.k1mb1.vkr_backend.subject.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Единое время на отметку (check-in) для всего предмета")
public record CheckInPolicyResponse(
        @Schema(description = "Включена ли единая политика окон check-in")
        boolean enabled,

        @Schema(description = "Длительность основного окна (секунды); null если политика выключена")
        Integer onTimeSeconds,

        @Schema(description = "Дополнительное окно для опоздавших (секунды); null если политика выключена")
        Integer lateSeconds) {}
