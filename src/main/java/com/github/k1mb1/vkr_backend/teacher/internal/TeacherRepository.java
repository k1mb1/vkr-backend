package com.github.k1mb1.vkr_backend.teacher.internal;

import com.github.k1mb1.vkr_backend.teacher.domain.Teacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {}
