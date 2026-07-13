package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID>, JpaSpecificationExecutor<SubjectEntity> {

    /** id групп, привязанных к предмету, — для порта group::api без выдачи сущностей наружу. */
    @Query("SELECT g.id FROM SubjectEntity s JOIN s.groups g WHERE s.id = :subjectId")
    List<UUID> findGroupIdsById(@Param("subjectId") UUID subjectId);
}
