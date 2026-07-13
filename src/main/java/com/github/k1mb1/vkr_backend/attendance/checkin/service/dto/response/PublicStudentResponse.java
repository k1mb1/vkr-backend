package com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(
        description = "Найденный по фамилии студент для публичной страницы check-in. "
                + "Отдаётся только для совпадений с поисковым запросом, без статуса посещаемости — "
                + "чтобы не раскрывать посторонним, кто пришёл, а кто прогулял.")
public record PublicStudentResponse(
        @Schema(description = "ID студента — нужен для последующей отметки (POST /{id}/check-in)")
        UUID id,

        @Schema(
                description = "Маскированное ФИО студента (фамилия + инициалы, напр. «Иванов И. И.») — "
                        + "публичный эндпоинт не отдаёт полное имя в целях защиты персональных данных")
        String username) {}
