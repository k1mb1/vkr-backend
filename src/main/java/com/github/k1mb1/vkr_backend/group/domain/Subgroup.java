package com.github.k1mb1.vkr_backend.group.domain;

import com.github.k1mb1.vkr_backend.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Hidden
@Entity
@Table(
    name = "subgroups", uniqueConstraints = @UniqueConstraint(
    name = "uk_subgroup_group_index", columnNames = { "group_id", "index" }
)
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subgroup
    extends BaseEntity {

    @Column(name = "index", nullable = false) Integer index;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    Group group;
}
