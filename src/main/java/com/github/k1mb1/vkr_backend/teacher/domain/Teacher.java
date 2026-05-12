package com.github.k1mb1.vkr_backend.teacher.domain;

import com.github.k1mb1.vkr_backend.common.domain.Auditable;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Hidden
@Entity
@Table(
    name = "teachers", uniqueConstraints = @UniqueConstraint(
    name = "uk_teachers_email", columnNames = { "email" }
)
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Teacher
    extends Auditable {

    @Id
    @Column(updatable = false, nullable = false)
    UUID id;

    @Column(nullable = false) String username;

    @Column(nullable = false, unique = true) String email;
}
