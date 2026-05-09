package com.github.k1mb1.vkr_backend.teacher.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "teachers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_teachers_email",
                columnNames = {"email"}
        )
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends BaseEntity {

    @Column(nullable = false)
    String username;

    @Column(nullable = false, unique = true)
    String email;
}
