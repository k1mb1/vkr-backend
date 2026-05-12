package com.github.k1mb1.vkr_backend.common.domain;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Hidden
@MappedSuperclass
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class ArchivableEntity
    extends BaseEntity {

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "archived_at", columnDefinition = "timestamptz")
    private Instant archivedAt;

    public boolean isArchived() {
        return archivedAt != null;
    }

    public void archive() {
        if (archivedAt == null) {
            this.archivedAt = Instant.now();
        }
    }

    public void unarchive() {
        this.archivedAt = null;
    }
}
