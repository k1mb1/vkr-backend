package com.github.k1mb1.vkr_backend.subject.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Операция бонуса за раннюю сдачу.
        ADD — к баллу прибавляется step за каждый шаг бонуса (балл + step * N).
        MULTIPLY — балл умножается на step за каждый шаг (балл * step^N), напр. step=1.1.
        Сам расчёт выполняется на фронте — бэкенд хранит только параметры.
        """)
public enum BonusOperation {
    ADD,
    MULTIPLY,
}
