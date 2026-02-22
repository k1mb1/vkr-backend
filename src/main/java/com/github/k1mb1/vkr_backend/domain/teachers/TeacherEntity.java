package com.github.k1mb1.vkr_backend.domain.teachers;

import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import com.github.k1mb1.vkr_backend.domain.AuditableBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "teachers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
@NamedEntityGraph(
        name = "Teacher.withSubjects",
        attributeNodes = @NamedAttributeNode("subjects")
)
public class TeacherEntity extends AuditableBase {

    @Column(nullable = false)
    @ToString.Include
    String username;

    @Column(nullable = false)
    @ToString.Include
    String email;


    @ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
    @Builder.Default
    Set<SubjectEntity> subjects = new HashSet<>();
}