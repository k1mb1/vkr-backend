package com.github.k1mb1.vkr_backend.grade.internal;

import com.github.k1mb1.vkr_backend.grade.domain.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface GradeRepository
    extends JpaRepository<Grade, UUID> {

    List<Grade> findByAssignmentIdInAndStudentIdIn(
        Collection<UUID> assignmentIds,
        Collection<UUID> studentIds
    );

    Optional<Grade> findByStudentIdAndAssignmentId(UUID studentId, UUID assignmentId);
}
