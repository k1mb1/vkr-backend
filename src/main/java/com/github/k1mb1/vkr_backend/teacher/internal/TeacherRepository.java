package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface TeacherRepository
    extends JpaRepository<Teacher, UUID> {}
