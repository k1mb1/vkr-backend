package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.student.domain.Student;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    List<Student> findByGroupId(UUID groupId);

    @EntityGraph("Student.withGroups")
    List<Student> findByGroupIdAndArchivedAtIsNull(UUID groupId);

    /** Батч-вариант для нескольких групп — чтобы не делать N запросов на группу. */
    @EntityGraph("Student.withGroups")
    List<Student> findByGroupIdInAndArchivedAtIsNull(Collection<UUID> groupIds);

    @EntityGraph("Student.withGroups")
    List<Student> findByGroupIdAndSubgroupIdAndArchivedAtIsNull(
        UUID groupId,
        UUID subgroupId
    );

    @Modifying
    @Query("DELETE FROM Student s WHERE s.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") UUID groupId);
}
