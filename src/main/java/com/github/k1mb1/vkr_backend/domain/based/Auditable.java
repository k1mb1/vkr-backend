package com.github.k1mb1.vkr_backend.domain.based;

import jakarta.persistence.*;
import java.time.Instant;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public abstract class Auditable {

    @CreatedDate
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "timestamp with time zone"
    )
    @ToString.Include
    protected Instant createdAt;

    @LastModifiedDate
    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "timestamp with time zone"
    )
    @ToString.Include
    protected Instant updatedAt;
}
