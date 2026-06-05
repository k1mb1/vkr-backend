package com.github.k1mb1.vkr_backend.results.web.responses;

import com.github.k1mb1.vkr_backend.attendance.web.responses.AttendanceTableResponse;
import com.github.k1mb1.vkr_backend.grading.web.responses.GradingTableResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
    description = "Единый ответ страницы итогов: таблица оценок и таблица посещаемости в одном " +
        "запросе. Политику итогов (банды) фронт берёт из grading.finalAssessmentPolicy."
)
public record ResultsResponse(
    @Schema(description = "Таблица оценок (с политиками штрафа/посещаемости/подсветки/итогов)")
    GradingTableResponse grading,

    @Schema(description = "Таблица посещаемости (поячеечная, с политикой подсветки)")
    AttendanceTableResponse attendance
) {}
