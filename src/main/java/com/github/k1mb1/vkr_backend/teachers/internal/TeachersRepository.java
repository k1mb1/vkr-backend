package com.github.k1mb1.vkr_backend.teachers.internal;

import com.github.k1mb1.vkr_backend.teachers.domain.Teacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface TeachersRepository
    extends JpaRepository<Teacher, UUID>, JpaSpecificationExecutor<Teacher> {}
