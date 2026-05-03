package com.github.k1mb1.vkr_backend.education.structure.internal;

import com.github.k1mb1.vkr_backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "students")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter @Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentEntity extends BaseEntity {
    @Column(nullable = false)
    @ToString.Include
    String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    StudentGroupEntity group;
}
