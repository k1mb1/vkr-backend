package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

// @SQLRestriction скрывает архивные subjects во всех SELECT'ах автоматически.
// Для доступа к архивным используй native query в репозитории.
@Hidden
@Entity
@Table(name = "subjects")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subject
    extends ArchivableEntity {

    @Column(nullable = false) String name;

    @Column(columnDefinition = "text") String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "subject_groups",
        joinColumns = @JoinColumn(name = "subject_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    Set<Group> groups = new HashSet<>();

    // Политика понижения за просрочку и бонуса за раннюю сдачу (опциональная фича).
    // Расчёт итогового балла делается на фронте — здесь только параметры.
    @Embedded
    @Builder.Default
    PenaltyPolicy penaltyPolicy = PenaltyPolicy.builder().build();

    // Связка посещаемости с баллом (опциональная фича). Расчёт на фронте.
    @Embedded
    @Builder.Default
    AttendancePolicy attendancePolicy = AttendancePolicy.builder().build();

    // Единое время на отметку (check-in) для всего предмета (опциональная фича).
    // Если включена — окна сессий берутся отсюда, а не из запроса на запуск.
    @Embedded
    @Builder.Default
    CheckInPolicy checkInPolicy = CheckInPolicy.builder().build();
}
