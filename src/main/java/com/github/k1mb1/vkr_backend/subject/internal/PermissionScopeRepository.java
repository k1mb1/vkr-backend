package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.PermissionScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionScopeRepository
    extends JpaRepository<PermissionScope, UUID> {

    @Query(
        """
            SELECT s FROM PermissionScope s
            JOIN FETCH s.permission p
            JOIN FETCH p.teacher
            JOIN FETCH p.subject
            JOIN FETCH s.group
            LEFT JOIN FETCH s.allowedSubgroup
            WHERE s.id = :id
            """
    )
    Optional<PermissionScope> findByIdWithDetails(
        @Param("id") UUID id
    );
}
