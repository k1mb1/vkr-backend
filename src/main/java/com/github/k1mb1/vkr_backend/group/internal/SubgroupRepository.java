package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SubgroupRepository extends JpaRepository<Subgroup, UUID> {

    List<Subgroup> findByGroupIdOrderByIndex(UUID groupId);
}
