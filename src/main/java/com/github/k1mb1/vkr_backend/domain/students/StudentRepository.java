package com.github.k1mb1.vkr_backend.domain.students;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository
    extends
        JpaRepository<StudentEntity, UUID>,
        JpaSpecificationExecutor<StudentEntity>
{
    List<StudentEntity> findAllByUsernameIn(List<String> usernames);

    List<StudentEntity> findAllBySubjects_Id(UUID subjectId);

    Page<StudentEntity> findAllBySubjects_Id(UUID subjectId, Pageable pageable);

    /** Batch-update group for a set of students — single UPDATE query. */
    @Modifying
    @Query("UPDATE StudentEntity s SET s.group = :group WHERE s.id IN :ids")
    void updateGroupForIds(
        @Param(
            "group"
        ) com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity group,
        @Param("ids") List<UUID> ids
    );
}
