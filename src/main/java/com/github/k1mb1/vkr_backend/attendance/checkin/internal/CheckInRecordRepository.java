package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface CheckInRecordRepository
    extends JpaRepository<CheckInRecord, UUID> {

    List<CheckInRecord> findBySessionId(UUID sessionId);

    Optional<CheckInRecord> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);
}
