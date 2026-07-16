package com.github.k1mb1.vkr_backend.teacher.repository;

import com.github.k1mb1.vkr_backend.teacher.domain.TeacherEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository
        extends JpaRepository<TeacherEntity, UUID>, JpaSpecificationExecutor<TeacherEntity> {}
