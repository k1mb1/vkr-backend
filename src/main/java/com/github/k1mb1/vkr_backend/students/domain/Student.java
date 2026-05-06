package com.github.k1mb1.vkr_backend.students.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.student_groups.domain.StudentGroup;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "students")
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class Student extends BaseEntity {

    @Column(nullable = false)
    @ToString.Include
    String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    StudentGroup group;
//TODO
//    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
//    @Builder.Default
//    Set<SubjectEntity> subjects = new HashSet<>();
}
