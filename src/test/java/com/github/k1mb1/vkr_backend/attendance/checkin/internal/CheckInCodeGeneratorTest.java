package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class CheckInCodeGeneratorTest {

    @Test
    void generatesSixCharCodeFromUnambiguousAlphabet() {
        var code = CheckInCodeGenerator.generate();

        assertThat(code).hasSize(6);
        // Без визуально неоднозначных символов (0/O/1/I/L) и только верхний регистр/цифры.
        assertThat(code).matches("[ABCDEFGHJKMNPQRSTUVWXYZ23456789]{6}");
    }

    @Test
    void generatesVariedCodes() {
        var codes = IntStream.range(0, 200)
                .mapToObj(i -> CheckInCodeGenerator.generate())
                .distinct()
                .count();

        // На 200 генерациях коллизий почти не бывает — допускаем небольшой запас.
        assertThat(codes).isGreaterThan(190);
    }
}
