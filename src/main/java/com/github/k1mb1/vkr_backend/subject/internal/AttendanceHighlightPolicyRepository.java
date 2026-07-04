package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.subject.domain.AttendanceHighlightPolicy;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceHighlightPolicyRepository extends JpaRepository<AttendanceHighlightPolicy, UUID> {}
