package com.github.k1mb1.vkr_backend.teachers.domain;

import com.github.k1mb1.vkr_backend.common.domain.Auditable;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "teachers")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class Teacher extends Auditable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    UUID id;

    @Column(nullable = false, unique = true)
    @ToString.Include
    String username;

    @Column(nullable = false, unique = true)
    @ToString.Include
    String email;

    //TODO
    //    @ManyToMany(mappedBy = "teachers", fetch = FetchType.LAZY)
    //    @Builder.Default
    //    Set<SubjectEntity> subjects = new HashSet<>();
}
