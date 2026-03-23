package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.domain.AuditableBase;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "student_groups")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentGroupEntity extends AuditableBase {

    @Column(nullable = false)
    @ToString.Include
    String name;

    @OneToMany(
        mappedBy = "group",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    Set<StudentEntity> students = new HashSet<>();
}
