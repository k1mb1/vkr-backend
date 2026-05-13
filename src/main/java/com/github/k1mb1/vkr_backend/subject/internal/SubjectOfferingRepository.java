package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.SubjectOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface SubjectOfferingRepository
    extends JpaRepository<SubjectOffering, UUID> {

    List<SubjectOffering> findBySubjectId(UUID subjectId);

    @Query(
        """
            SELECT o FROM SubjectOffering o
            JOIN FETCH o.group
            WHERE o.subject.id = :subjectId
            ORDER BY o.group.name
            """
    )
    List<SubjectOffering> findBySubjectIdFetchGroup(
        @Param("subjectId") UUID subjectId
    );
}
