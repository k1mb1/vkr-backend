package com.github.k1mb1.vkr_backend.domain.teachers;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TeacherRepository
    extends
        JpaRepository<TeacherEntity, UUID>,
        JpaSpecificationExecutor<TeacherEntity> {}
