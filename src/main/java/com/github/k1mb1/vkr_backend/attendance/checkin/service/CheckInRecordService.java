package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInRecordRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.PublicCheckInRecordResponse;
import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.group.repository.StudentRepository;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInRecordService {

    final CheckInSessionRepository sessionRepository;

    final CheckInRecordRepository recordRepository;

    final StudentRepository studentRepository;

    final LessonStudentsApi lessonStudentsApi;

    @Transactional
    public PublicCheckInRecordResponse checkIn(UUID sessionId, StudentCheckInRequest request) {
        var session = sessionRepository
                .findWithDetailsById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("CheckInSession", sessionId));

        var now = Instant.now();
        var state = session.stateAt(now);
        if (state != CheckInSessionState.OPEN && state != CheckInSessionState.LATE_WINDOW) {
            throw new ConflictException("Check-in is closed for session: " + sessionId);
        }

        // Код аудитории проверяем раньше, чем принадлежность студента,
        // чтобы ошибка кода не работала оракулом «существует ли такой studentId».
        if (!CheckInCodes.matches(session.getCode(), request.code())) {
            throw new IllegalArgumentException("Неверный код сессии");
        }

        var studentId = request.studentId();
        var students = lessonStudentsApi.studentsOf(session.getLessonScope());
        var inScope = students.stream().anyMatch(s -> s.getId().equals(studentId));
        if (!inScope) {
            throw new IllegalArgumentException("Student is not part of this lesson audience: " + studentId);
        }

        var status = session.statusForCheckInAt(now);
        if (status == null) {
            throw new ConflictException("Check-in window has elapsed");
        }

        var record = recordRepository
                .findBySessionIdAndStudentId(sessionId, studentId)
                .orElseGet(() -> CheckInRecordEntity.builder()
                        .session(session)
                        .student(studentRepository.getReferenceById(studentId))
                        .checkedInAt(now)
                        .build());

        // first check-in wins; do not downgrade PRESENT to LATE on repeated submission
        if (record.getId() == null) {
            record.setStatus(status);
            record.setCheckedInAt(now);
        }

        var saved = recordRepository.save(record);
        // Отдаём только собственный результат студента — без ID записи и данных других.
        return PublicCheckInRecordResponse.builder()
                .status(saved.getStatus())
                .checkedInAt(saved.getCheckedInAt())
                .build();
    }
}
