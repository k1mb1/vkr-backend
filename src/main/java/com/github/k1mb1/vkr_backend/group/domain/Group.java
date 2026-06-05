package com.github.k1mb1.vkr_backend.group.domain;

import com.github.k1mb1.vkr_backend.common.domain.ArchivableEntity;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Hidden
@Entity
@Table(name = "groups")
@SQLRestriction("archived_at IS NULL")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Group extends ArchivableEntity {

    @Column(nullable = false)
    String name;

    @OneToMany(
        mappedBy = "group",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @org.hibernate.annotations.BatchSize(size = 50)
    @Builder.Default
    Set<Subgroup> subgroups = new HashSet<>();

    @OneToMany(mappedBy = "group")
    @org.hibernate.annotations.BatchSize(size = 50)
    @Builder.Default
    Set<Student> students = new HashSet<>();
}
