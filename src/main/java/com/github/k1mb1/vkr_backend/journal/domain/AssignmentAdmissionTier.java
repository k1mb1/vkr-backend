package com.github.k1mb1.vkr_backend.journal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Уровень (tier) допуска задания в режиме {@code TIERED}.
 * <p>
 * Список tiers в задании хранится по убыванию старшинства (первая — самая старшая оценка).
 * Фронт выбирает первую подходящую tier для конкретного студента
 * ({@code score >= minScore}) — это его «потолок» по данному заданию;
 * все более старшие tiers считаются непройденными.
 * <p>
 * Вместо текстового label используется ссылка {@code bandId} на {@link com.github.k1mb1.vkr_backend.subject.domain.AssessmentBandEntity},
 * что исключает дублирование строк и гарантирует консистентность с бандами политики итогов.
 */
@Embeddable
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentAdmissionTier {

    /** Ссылка на банду итоговой аттестации ({@code AssessmentBandEntity.id}). */
    @Column(name = "band_id", nullable = false)
    UUID bandId;

    /** Минимальный балл за это задание (включительно) для допуска к данной банде. */
    @Column(name = "min_score")
    Integer minScore;
}
