package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface SubgroupRepository
    extends JpaRepository<Subgroup, UUID> {

    List<Subgroup> findByGroupIdOrderByIndex(UUID groupId);
}
