package com.github.k1mb1.vkr_backend.subject.repository;

import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicyEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInPolicyRepository extends JpaRepository<CheckInPolicyEntity, UUID> {}
