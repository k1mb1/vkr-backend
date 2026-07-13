package com.github.k1mb1.vkr_backend.grading.repository;

import com.github.k1mb1.vkr_backend.grading.domain.GradeEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<GradeEntity, UUID> {
    @EntityGraph("Grade.withDetails")
    List<GradeEntity> findByLessonIdInAndStudentIdIn(Collection<UUID> lessonIds, Collection<UUID> studentIds);
}
