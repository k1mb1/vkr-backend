package com.github.k1mb1.vkr_backend.attendance.checkin.repository;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecordEntity, UUID> {
    @EntityGraph("CheckInRecord.withStudent")
    List<CheckInRecordEntity> findBySessionId(UUID sessionId);

    Optional<CheckInRecordEntity> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);
}
