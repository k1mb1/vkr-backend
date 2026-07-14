package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.subject.domain.TeacherEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository
        extends JpaRepository<TeacherEntity, UUID>, JpaSpecificationExecutor<TeacherEntity> {}
