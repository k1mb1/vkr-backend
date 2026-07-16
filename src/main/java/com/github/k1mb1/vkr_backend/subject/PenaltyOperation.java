package com.github.k1mb1.vkr_backend.subject;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Операция понижения балла за просрочку.
        SUBTRACT — из балла вычитается step за каждое понижение (балл - step * N).
        MULTIPLY — балл умножается на step за каждое понижение (балл * step^N), напр. step=0.5.
        Сам расчёт выполняется на фронте — бэкенд хранит только параметры политики.
        """)
public enum PenaltyOperation {
    SUBTRACT,
    MULTIPLY,
}
