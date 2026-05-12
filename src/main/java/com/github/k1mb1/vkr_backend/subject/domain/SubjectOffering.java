package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// "Этот предмет читается этой группе в таком-то семестре"
// Заменяет @ManyToMany Subject <-> Group
@Hidden
@Entity
@Table(
    name = "subject_offerings", uniqueConstraints = @UniqueConstraint(
    name = "uk_offering_subject_group_period", columnNames = { "subject_id", "group_id" }
), indexes = {
    @Index(name = "idx_offering_subject", columnList = "subject_id"), @Index(
    name = "idx_offering_group", columnList = "group_id"
),
}
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubjectOffering
    extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;

    //    @Column(name = "academic_year", nullable = false)
    //    Integer academicYear;
    //
    //    @Column(nullable = false)
    //    Integer semester;
    //
    //    @Column(name = "is_active", nullable = false)
    //    @Builder.Default
    //    Boolean active = true;
}
