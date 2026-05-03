package com.github.k1mb1.vkr_backend.education.attendance.internal;

import com.github.k1mb1.vkr_backend.shared.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.education.attendance.api.PresenceType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "student_attendances")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter @Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentAttendanceEntity extends BaseEntity {

    @Column(name = "lesson_id", nullable = false)
    UUID lessonId;

    @Column(name = "student_id", nullable = false)
    UUID studentId;

    @Column(name = "presence", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    PresenceType presence = PresenceType.NONE;

    String note;
}
