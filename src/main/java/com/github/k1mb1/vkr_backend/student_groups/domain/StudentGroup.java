package com.github.k1mb1.vkr_backend.student_groups.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import com.github.k1mb1.vkr_backend.students.domain.Student;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "student_groups")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class StudentGroup extends BaseEntity {

    @Column(nullable = false)
    @ToString.Include
    String name;

    /**
     * Parent group reference. NULL = this is a main group.
     * Non-null = this entity is a subgroup of the referenced group.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group_id")
    StudentGroup parentGroup;

    @OneToMany(
        mappedBy = "parentGroup",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    Set<StudentGroup> subgroups = new HashSet<>();

    @OneToMany(
        mappedBy = "group",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    Set<Student> students = new HashSet<>();

    @Formula(
        "(select count(*) from student_groups sg where sg.parent_group_id = id)"
    )
    int subgroupCount;

    @Formula(
        "(select count(*) from students s left join student_groups sg on s.group_id = sg.id where s.group_id = id or sg.parent_group_id = id)"
    )
    int totalStudentCount;

    //TODO Formula вызывается при каждом обращении к сущности, даже если не нужно получать эти поля. Возможно стоит убрать их из сущности и делать отдельными запросами при необходимости.
}
