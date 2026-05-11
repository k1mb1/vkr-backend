package com.github.k1mb1.vkr_backend.common.domain;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Hidden
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class Auditable {

    @CreatedDate
    @Column(
        nullable = false,
        updatable = false,
        columnDefinition = "timestamptz"
    )
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;
}
