package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.subject.domain.PenaltyPolicyEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PenaltyPolicyRepository extends JpaRepository<PenaltyPolicyEntity, UUID> {}
