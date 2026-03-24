package com.github.k1mb1.vkr_backend.domain.teachers;

import com.github.k1mb1.vkr_backend.domain.based.Auditable;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "teachers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
@NamedEntityGraph(
    name = "Teacher.withSubjects",
    attributeNodes = @NamedAttributeNode("subjects")
)
public class TeacherEntity extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    UUID id;

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
