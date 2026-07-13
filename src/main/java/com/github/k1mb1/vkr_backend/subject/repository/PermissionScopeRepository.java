package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.subject.domain.PermissionScopeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionScopeRepository extends JpaRepository<PermissionScopeEntity, UUID> {

    @Query("""
            SELECT s FROM PermissionScopeEntity s
            JOIN FETCH s.permission p
            JOIN FETCH p.teacher
            JOIN FETCH p.subject
            JOIN FETCH s.group
            LEFT JOIN FETCH s.allowedSubgroup
            WHERE s.id = :id
            """)
    Optional<PermissionScopeEntity> findByIdWithDetails(@Param("id") UUID id);
}
