package com.github.k1mb1.vkr_backend.domain.student_attendances;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StudentAttendanceRepository extends JpaRepository<StudentAttendanceEntity, UUID>, JpaSpecificationExecutor<StudentAttendanceEntity> {
}