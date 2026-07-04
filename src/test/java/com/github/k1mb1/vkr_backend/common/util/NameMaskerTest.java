package com.github.k1mb1.vkr_backend.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NameMaskerTest {

    @ParameterizedTest
    @CsvSource({
        "Иванов Иван Иванович, Иванов И. И.",
        "Петров Пётр, Петров П.",
        "Сидоров, Сидоров",
        "Ким Ир Сен, Ким И. С.",
    })
    void masksFullNameToSurnameAndInitials(String input, String expected) {
        assertThat(NameMasker.maskFullName(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void returnsBlankInputUnchanged(String input) {
        assertThat(NameMasker.maskFullName(input)).isEqualTo(input);
    }

    @Test
    void collapsesExtraWhitespaceBetweenParts() {
        assertThat(NameMasker.maskFullName("  Иванов   Иван   Иванович  ")).isEqualTo("Иванов И. И.");
    }
}
