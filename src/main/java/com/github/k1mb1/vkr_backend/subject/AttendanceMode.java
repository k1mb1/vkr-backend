package com.github.k1mb1.vkr_backend.subject;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = """
        Как посещаемость участвует в итоге.
        COMBINED — посещаемость вливается в итоговый балл через AttendancePolicy (баллы за статусы),
        и пороги автомата/полос/зачёта работают по этому общему баллу.
        SEPARATE — посещаемость в балл не входит, а проверяется отдельным требованием-гейтом
        (процент или количество посещений). Не прошёл гейт — не допущен / не зачтено независимо
        от баллов. Сам расчёт выполняет фронт.
        """)
public enum AttendanceMode {
    COMBINED,
    SEPARATE,
}
