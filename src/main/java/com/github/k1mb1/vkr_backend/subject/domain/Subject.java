package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

// @SQLRestriction скрывает архивные subjects во всех SELECT'ах автоматически.
// Для доступа к архивным используй native query в репозитории.
@Entity
@Table(name = "subjects")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subject extends ArchivableEntity {

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "text")
    String description;
}
