package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

// @SQLRestriction скрывает архивные subjects во всех SELECT'ах автоматически.
// Для доступа к архивным используй native query в репозитории.
@Hidden
@Entity
@Table(name = "subjects")
@SQLRestriction("archived_at IS NULL")
@NamedEntityGraph(name = "Subject.withGroups", attributeNodes = @NamedAttributeNode("groups"))
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEntity extends ArchivableEntity {

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "text")
    String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subject_groups",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    @BatchSize(size = 50)
    @Builder.Default
    Set<GroupEntity> groups = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "penalty_policy_id", nullable = false)
    @Builder.Default
    PenaltyPolicyEntity penaltyPolicy = PenaltyPolicyEntity.builder().build();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "attendance_policy_id", nullable = false)
    @Builder.Default
    AttendancePolicyEntity attendancePolicy = AttendancePolicyEntity.builder().build();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "checkin_policy_id", nullable = false)
    @Builder.Default
    CheckInPolicyEntity checkInPolicy = CheckInPolicyEntity.builder().build();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "grading_highlight_policy_id", nullable = false)
    @Builder.Default
    GradingHighlightPolicyEntity gradingHighlightPolicy =
            GradingHighlightPolicyEntity.builder().build();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "attendance_highlight_policy_id", nullable = false)
    @Builder.Default
    AttendanceHighlightPolicyEntity attendanceHighlightPolicy =
            AttendanceHighlightPolicyEntity.builder().build();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "final_assessment_policy_id", nullable = false)
    @Builder.Default
    FinalAssessmentPolicyEntity finalAssessmentPolicy =
            FinalAssessmentPolicyEntity.builder().build();
}
