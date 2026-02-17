package com.github.k1mb1.vkr_backend.domain.grades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface GradeRepository extends JpaRepository<GradeEntity, UUID>, JpaSpecificationExecutor<GradeEntity> {
}