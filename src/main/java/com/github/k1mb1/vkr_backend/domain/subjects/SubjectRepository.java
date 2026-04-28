package com.github.k1mb1.vkr_backend.domain.subjects;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubjectRepository
    extends
        JpaRepository<SubjectEntity, UUID>,
        JpaSpecificationExecutor<SubjectEntity>
{
}
