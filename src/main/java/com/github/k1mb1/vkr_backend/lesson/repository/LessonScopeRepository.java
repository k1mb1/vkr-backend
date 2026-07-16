package com.github.k1mb1.vkr_backend.lesson.repository;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonScopeRepository extends JpaRepository<LessonScopeEntity, UUID> {

    @EntityGraph(attributePaths = {"lesson", "lesson.subject", "group", "allowedSubgroup"})
    Optional<LessonScopeEntity> findWithDetailsById(UUID id);

    /** Scope'ы с аудиторией (группы предмета нужны для allGroups) — для вычисления ростера. */
    @EntityGraph(attributePaths = {"lesson", "lesson.subject", "lesson.subject.groups", "group", "allowedSubgroup"})
    List<LessonScopeEntity> findWithAudienceByIdIn(Collection<UUID> ids);

    /** id предметов, которым принадлежат указанные scope'ы — для проверки доступа на запись. */
    @Query("SELECT DISTINCT ls.lesson.subject.id FROM LessonScopeEntity ls WHERE ls.id IN :ids")
    Set<UUID> findSubjectIdsByScopeIds(Collection<UUID> ids);
}
