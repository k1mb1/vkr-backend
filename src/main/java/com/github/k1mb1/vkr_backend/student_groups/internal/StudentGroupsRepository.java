package com.github.k1mb1.vkr_backend.student_groups.internal;

import com.github.k1mb1.vkr_backend.student_groups.domain.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

interface StudentGroupsRepository
        extends
        JpaRepository<StudentGroup, UUID>,
        JpaSpecificationExecutor<StudentGroup> {
}
