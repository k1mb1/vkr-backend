package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "student_groups")
@NoArgsConstructor
@Getter @Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentGroupEntity extends BaseEntity {
    @Column(nullable = false)
    @ToString.Include
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group_id")
    StudentGroupEntity parentGroup;

    @OneToMany(mappedBy = "parentGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<StudentGroupEntity> subgroups = new HashSet<>();

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<StudentEntity> students = new HashSet<>();
}
