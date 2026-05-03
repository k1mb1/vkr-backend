package com.github.k1mb1.vkr_backend.education.structure.internal;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<StudentEntity, UUID>, JpaSpecificationExecutor<StudentEntity> {
    @Query("SELECT s.id FROM StudentEntity s WHERE s.group.id = :groupId OR s.group.parentGroup.id = :groupId")
    List<UUID> findAllIdsByGroupId(@Param("groupId") UUID groupId);

    List<StudentEntity> findAllByIdIn(Collection<UUID> ids);
}
