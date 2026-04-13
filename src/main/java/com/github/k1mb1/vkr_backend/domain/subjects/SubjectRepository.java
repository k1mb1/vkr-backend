package com.github.k1mb1.vkr_backend.domain.subjects;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubjectRepository
    extends
        JpaRepository<SubjectEntity, UUID>,
        JpaSpecificationExecutor<SubjectEntity>
{
    @Query(
        """
        SELECT DISTINCT s FROM SubjectEntity s
        LEFT JOIN FETCH s.students st
        LEFT JOIN FETCH st.group g
        LEFT JOIN FETCH g.parentGroup
        WHERE s.id = :id
        """
    )
    Optional<SubjectEntity> findByIdWithStudentsAndGroups(@Param("id") UUID id);
}
