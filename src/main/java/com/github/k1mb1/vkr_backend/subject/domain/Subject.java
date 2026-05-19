package com.github.k1mb1.vkr_backend.subject.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

// @SQLRestriction скрывает архивные subjects во всех SELECT'ах автоматически.
// Для доступа к архивным используй native query в репозитории.
@Hidden
@Entity
@Table(name = "subjects")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subject
    extends ArchivableEntity {

    @Column(nullable = false) String name;

    @Column(columnDefinition = "text") String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "subject_groups",
        joinColumns = @JoinColumn(name = "subject_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    @Builder.Default
    Set<Group> groups = new HashSet<>();
}
