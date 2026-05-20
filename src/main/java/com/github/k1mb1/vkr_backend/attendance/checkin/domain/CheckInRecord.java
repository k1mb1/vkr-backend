package com.github.k1mb1.vkr_backend.attendance.checkin.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Hidden
@Entity
@Table(
    name = "check_in_records", uniqueConstraints = @UniqueConstraint(
    name = "uk_check_in_records_session_student", columnNames = { "session_id", "student_id" }
), indexes = {
    @Index(name = "idx_check_in_records_session", columnList = "session_id"),
    @Index(name = "idx_check_in_records_student", columnList = "student_id"),
}
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRecord
    extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    CheckInSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    Student student;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "check_in_record_status")
    CheckInRecordStatus status;

    @Column(
        name = "checked_in_at",
        nullable = false,
        columnDefinition = "timestamptz"
    ) Instant checkedInAt;
}
