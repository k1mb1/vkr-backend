package com.github.k1mb1.vkr_backend.domain.students;

import com.github.k1mb1.vkr_backend.domain.AuditableBase;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "students")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentEntity extends AuditableBase {

    @Column(nullable = false)
    @ToString.Include
    String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    StudentGroupEntity group;

    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    @Builder.Default
    Set<SubjectEntity> subjects = new HashSet<>();
}
