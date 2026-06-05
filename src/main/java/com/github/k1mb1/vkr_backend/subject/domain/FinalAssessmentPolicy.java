package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Параметры промежуточной аттестации (итогов семестра), привязанные к предмету. Отвечает на
 * вопросы «какой ярлык/оценку получит студент» и «сколько баллов/задач до следующего».
 * <p>
 * Модель — единый список банд (без разделения на зачёт/экзамен): зачёт это одна банда «зачтено»,
 * экзамен — несколько («5», «4», «3»). Бэкенд хранит банды и режим посещаемости, расчёт по
 * студенту делает фронт (итоговый балл {@code total} = сумма за задания
 * [+ вклад посещаемости, если COMBINED] − штраф + бонус):
 * <pre>
 *   if (!enabled) -> блок итогов не показываем
 *
 *   // посещаемость-гейт (см. ниже): не пройден -> вердикта нет (не допущен)
 *   if (attendanceMode == SEPARATE && !gateOk) -> не допущен
 *
 *   // вердикт = первая подходящая банда (банды по убыванию старшинства):
 *   подходит(band) = (band.minPoints == null    || total >= band.minPoints)
 *                 && (band.requiredTasks == null || закрытоОбязательныхЗадач >= band.requiredTasks)
 *   если ни одна не подошла — без вердикта (например «не зачтено» / «неуд»)
 *
 *   // «сколько ещё»: до ближайшей более старшей банды по баллам = band.minPoints - total
 * </pre>
 * Посещаемость через {@code attendanceMode}:
 * <pre>
 *   COMBINED — посещаемость уже в total через AttendancePolicy (поведение по умолчанию);
 *   SEPARATE — посещаемость в total НЕ входит, а проверяется отдельным гейтом-допуском:
 *     attended = сумма посещений по статусам, включённым флагами attendanceCount* (из сводки
 *                посещаемости таблицы оценок: present/late/absent/excused)
 *     gateOk   = PERCENT ? (lessons > 0 && attended * 100 / lessons >= attendanceMinPercent)
 *                        : attended >= attendanceMinCount
 * </pre>
 */
@Hidden
@Entity
@Table(name = "final_assessment_policies")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FinalAssessmentPolicy extends BaseEntity {

    /** Опциональная фича: показываются ли итоги (банды) для предмета. */
    @Builder.Default
    @Column(nullable = false)
    boolean enabled = false;

    /** Банды итоговой аттестации по убыванию старшинства. Пусто при enabled = false. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "final_assessment_bands",
        joinColumns = @JoinColumn(name = "policy_id")
    )
    @OrderColumn(name = "position")
    @Builder.Default
    List<AssessmentBand> bands = new ArrayList<>();

    /** Учёт посещаемости: в общий балл (COMBINED) или отдельным гейтом (SEPARATE). */
    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    @Builder.Default
    AttendanceMode attendanceMode = AttendanceMode.COMBINED;

    /** SEPARATE: чем меряется гейт — PERCENT или COUNT. Заполняется при attendanceMode = SEPARATE. */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    AttendanceRequirementMode attendanceRequirementMode;

    /** SEPARATE + PERCENT: минимальный процент посещённых занятий (0..100). */
    @Column
    Integer attendanceMinPercent;

    /** SEPARATE + COUNT: минимальное количество посещённых занятий. */
    @Column
    Integer attendanceMinCount;

    /** SEPARATE: засчитывать ли PRESENT как посещение. */
    @Builder.Default
    @Column(nullable = false)
    boolean attendanceCountPresent = false;

    /** SEPARATE: засчитывать ли LATE (опоздание) как посещение. */
    @Builder.Default
    @Column(nullable = false)
    boolean attendanceCountLate = false;

    /** SEPARATE: засчитывать ли ABSENT (пропуск без причины) как посещение. */
    @Builder.Default
    @Column(nullable = false)
    boolean attendanceCountAbsent = false;

    /** SEPARATE: засчитывать ли EXCUSED (пропуск по уважительной причине) как посещение. */
    @Builder.Default
    @Column(nullable = false)
    boolean attendanceCountExcused = false;
}
