package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.Auditable;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Hidden
@Entity
@Table(name = "teachers")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEntity extends Auditable {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "archived_at", columnDefinition = "timestamptz")
    private Instant archivedAt;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, columnDefinition = "citext")
    private String email;
}
