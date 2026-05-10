package com.github.k1mb1.vkr_backend.group.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "subgroups",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_subgroup_group_index",
        columnNames = { "group_id", "index" }
    )
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subgroup extends BaseEntity {

    @Column(name = "index", nullable = false)
    Integer index;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;
}
