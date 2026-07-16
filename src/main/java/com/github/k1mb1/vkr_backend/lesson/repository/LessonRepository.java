package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.subject.LessonType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, UUID>, JpaSpecificationExecutor<LessonEntity> {
    @EntityGraph(
            attributePaths = {
                "subject",
                "scopes",
                "scopes.group",
                "scopes.allowedSubgroup",
            })
    Optional<LessonEntity> findWithDetailsById(UUID id);

    @Query("""
        SELECT MAX(l.orderIndex) FROM LessonEntity l
        WHERE l.subject.id = :subjectId AND l.type = :type
        """)
    Integer findMaxOrderIndex(@Param("subjectId") UUID subjectId, @Param("type") LessonType type);

    @Modifying
    @Query("""
        UPDATE LessonEntity l SET l.orderIndex = l.orderIndex - 1
        WHERE l.subject.id = :subjectId
          AND l.type = :type
          AND l.orderIndex > :afterIndex
        """)
    int shiftOrderIndexDown(
            @Param("subjectId") UUID subjectId, @Param("type") LessonType type, @Param("afterIndex") int afterIndex);

    @Modifying
    @Query("""
        UPDATE LessonEntity l SET l.active = false
        WHERE l.subject.id = :subjectId AND l.type = :type AND l.active = true
        """)
    int clearActiveForSubjectAndType(@Param("subjectId") UUID subjectId, @Param("type") LessonType type);

    Optional<LessonEntity> findBySubjectIdAndTypeAndActiveTrue(UUID subjectId, LessonType type);

    /** findAll по спецификации с графом Lesson.withDetails (замена бывшего кастомного findAllWithDetails). */
    @Override
    @EntityGraph("Lesson.withDetails")
    List<LessonEntity> findAll(org.springframework.data.jpa.domain.Specification<LessonEntity> spec);

    /** id предметов указанных занятий — для проверки доступа на запись. */
    @Query("SELECT DISTINCT l.subject.id FROM LessonEntity l WHERE l.id IN :ids")
    java.util.Set<UUID> findSubjectIdsByLessonIds(java.util.Collection<UUID> ids);
}
