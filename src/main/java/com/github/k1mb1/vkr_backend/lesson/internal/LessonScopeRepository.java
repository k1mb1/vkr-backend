package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonScopeRepository extends JpaRepository<LessonScope, UUID> {

    @EntityGraph(attributePaths = {"lesson", "lesson.subject", "group", "allowedSubgroup"})
    Optional<LessonScope> findWithDetailsById(UUID id);

    /** id предметов, которым принадлежат указанные scope'ы — для проверки доступа на запись. */
    @Query("SELECT DISTINCT ls.lesson.subject.id FROM LessonScope ls WHERE ls.id IN :ids")
    Set<UUID> findSubjectIdsByScopeIds(Collection<UUID> ids);
}
