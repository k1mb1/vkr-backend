package com.github.k1mb1.vkr_backend.group.repository;

import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubgroupRepository extends JpaRepository<SubgroupEntity, UUID> {

    List<SubgroupEntity> findByGroupIdOrderByIndex(UUID groupId);
}
