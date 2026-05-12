package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.student.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface StudentRepository
    extends JpaRepository<Student, UUID> {
    List<Student> findByGroupId(UUID groupId);

    List<Student> findByGroupIdAndArchivedAtIsNull(UUID groupId);

    @Modifying
    @Query("DELETE FROM Student s WHERE s.group.id = :groupId")
    void deleteByGroupId(
        @Param("groupId") UUID groupId
    );
}
