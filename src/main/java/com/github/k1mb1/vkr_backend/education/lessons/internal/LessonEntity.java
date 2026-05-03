package com.github.k1mb1.vkr_backend.education.lessons.internal;

import com.github.k1mb1.vkr_backend.shared.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.education.lessons.api.IssuanceMode;
import com.github.k1mb1.vkr_backend.education.lessons.api.LessonType;
import com.github.k1mb1.vkr_backend.education.lessons.api.PenaltyMode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "lessons")
@NoArgsConstructor
@Getter @Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class LessonEntity extends BaseEntity {

    @Column(nullable = false)
    @ToString.Include
    String name;

    OffsetDateTime dateTime;

    @Column(name = "subject_id", nullable = false)
    UUID subjectId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    LessonType type = LessonType.NONE;

    @Column(name = "group_id")
    UUID groupId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    IssuanceMode issuanceMode = IssuanceMode.AUTO;

    @Column(name = "issued_at")
    Instant issuedAt;

    @Column(nullable = false)
    @Builder.Default
    int issuedTaskIndex = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    PenaltyMode penaltyMode = PenaltyMode.NONE;

    @Column(nullable = false, precision = 5, scale = 4)
    @Builder.Default
    BigDecimal penaltyStep = new BigDecimal("0.25");
}
