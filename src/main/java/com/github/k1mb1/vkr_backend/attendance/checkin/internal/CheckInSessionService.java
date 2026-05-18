package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StudentCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInRecordResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CheckInSessionService
    implements CheckInSessionApi {

    final CheckInSessionRepository sessionRepository;

    final CheckInRecordRepository recordRepository;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonRepository lessonRepository;

    final StudentRepository studentRepository;

    final CheckInSessionMapper mapper;

    final AttendanceApi attendanceApi;

    @Transactional
    @Override
    public CheckInSessionResponse start(StartCheckInRequest request) {
        var permission = permissionRepository.findById(request.permissionId())
            .orElseThrow(() -> new EntityNotFoundException("TeacherSubjectPermission not found: " + request.permissionId()));
        var lesson = lessonRepository.findByIdWithDetails(request.lessonId())
            .orElseThrow(() -> new EntityNotFoundException("Lesson not found: " + request.lessonId()));

        assertLessonMatchesPermission(lesson, permission);

        sessionRepository.findByLessonIdAndConfirmedAtIsNullAndCancelledAtIsNull(lesson.getId())
            .ifPresent(existing -> {
                throw new IllegalStateException(
                    "Active check-in session already exists for lesson: " + lesson.getId());
            });

        var session = CheckInSession.builder()
            .lesson(lesson)
            .permission(permission)
            .startedAt(Instant.now())
            .onTimeSeconds(request.onTimeSeconds())
            .lateSeconds(request.lateSeconds())
            .build();

        return mapper.toResponse(sessionRepository.save(session), Instant.now());
    }

    @Override
    public CheckInSessionResponse get(UUID sessionId) {
        var session = loadSession(sessionId);
        return mapper.toResponse(session, Instant.now());
    }

    @Override
    public List<CheckInSessionResponse> listForPermission(UUID permissionId) {
        var now = Instant.now();
        return sessionRepository.findByPermissionIdOrderByStartedAtDesc(permissionId)
            .stream()
            .map(s -> mapper.toResponse(s, now))
            .toList();
    }

    @Override
    public CheckInPreviewResponse preview(UUID sessionId) {
        var session = loadSession(sessionId);
        var students = loadStudents(session.getPermission());
        var recordsByStudent = recordsByStudentId(sessionId);

        var rows = students.stream()
            .map(student -> {
                var record = recordsByStudent.get(student.getId());
                var checkInStatus = record != null ? record.getStatus() : null;
                var checkedInAt = record != null ? record.getCheckedInAt() : null;
                var proposed = proposedAttendanceStatus(checkInStatus);
                return new CheckInPreviewResponse.Row(
                    student.getId(),
                    student.getUsername(),
                    checkInStatus,
                    checkedInAt,
                    proposed
                );
            })
            .toList();

        return new CheckInPreviewResponse(mapper.toResponse(session, Instant.now()), rows);
    }

    @Transactional
    @Override
    public CheckInSessionResponse confirm(UUID sessionId, ConfirmCheckInRequest request) {
        var session = loadSession(sessionId);
        if (session.getConfirmedAt() != null) {
            throw new IllegalStateException("Session already confirmed: " + sessionId);
        }
        if (session.getCancelledAt() != null) {
            throw new IllegalStateException("Session is cancelled: " + sessionId);
        }

        var students = loadStudents(session.getPermission());
        var studentIds = students.stream().map(Student::getId).collect(java.util.stream.Collectors.toSet());
        var recordsByStudent = recordsByStudentId(sessionId);

        var overridesByStudent = new HashMap<UUID, ConfirmCheckInRequest.Override>();
        if (request != null && request.overrides() != null) {
            for (var ov : request.overrides()) {
                if (!studentIds.contains(ov.studentId())) {
                    throw new IllegalArgumentException(
                        "Override references student not in lesson scope: " + ov.studentId());
                }
                overridesByStudent.put(ov.studentId(), ov);
            }
        }

        var lessonId = session.getLesson().getId();
        for (var student : students) {
            var override = overridesByStudent.get(student.getId());
            AttendanceStatus status;
            String comment;
            if (override != null) {
                status = override.status();
                comment = override.comment();
            } else {
                var record = recordsByStudent.get(student.getId());
                status = proposedAttendanceStatus(record != null ? record.getStatus() : null);
                comment = null;
            }
            attendanceApi.upsert(new UpsertAttendanceRequest(student.getId(), lessonId, status, comment));
        }

        session.setConfirmedAt(Instant.now());
        return mapper.toResponse(sessionRepository.save(session), Instant.now());
    }

    @Transactional
    @Override
    public CheckInSessionResponse cancel(UUID sessionId) {
        var session = loadSession(sessionId);
        if (session.getConfirmedAt() != null) {
            throw new IllegalStateException("Cannot cancel a confirmed session: " + sessionId);
        }
        if (session.getCancelledAt() == null) {
            session.setCancelledAt(Instant.now());
            sessionRepository.save(session);
        }
        return mapper.toResponse(session, Instant.now());
    }

    @Override
    public PublicCheckInSessionResponse getPublic(UUID sessionId) {
        var session = loadSession(sessionId);
        var students = loadStudents(session.getPermission());
        var recordsByStudent = recordsByStudentId(sessionId);
        var now = Instant.now();

        var rows = students.stream()
            .map(student -> {
                var record = recordsByStudent.get(student.getId());
                return new PublicCheckInSessionResponse.Student(
                    student.getId(),
                    student.getUsername(),
                    record != null ? record.getStatus() : null,
                    record != null ? record.getCheckedInAt() : null
                );
            })
            .toList();

        return new PublicCheckInSessionResponse(
            session.getId(),
            session.getLesson().getTopic(),
            session.stateAt(now),
            session.onTimeEndsAt(),
            session.lateEndsAt(),
            now,
            rows
        );
    }

    @Transactional
    @Override
    public CheckInRecordResponse checkIn(UUID sessionId, StudentCheckInRequest request) {
        var session = loadSession(sessionId);
        var now = Instant.now();
        var state = session.stateAt(now);
        if (state != CheckInSessionState.OPEN && state != CheckInSessionState.LATE_WINDOW) {
            throw new IllegalStateException("Check-in is closed for session: " + sessionId);
        }

        var studentId = request.studentId();
        var students = loadStudents(session.getPermission());
        var inScope = students.stream().anyMatch(s -> s.getId().equals(studentId));
        if (!inScope) {
            throw new IllegalArgumentException(
                "Student is not part of this lesson scope: " + studentId);
        }

        var status = session.statusForCheckInAt(now);
        if (status == null) {
            throw new IllegalStateException("Check-in window has elapsed");
        }

        var record = recordRepository.findBySessionIdAndStudentId(sessionId, studentId)
            .orElseGet(() -> CheckInRecord.builder()
                .session(session)
                .student(studentRepository.getReferenceById(studentId))
                .checkedInAt(now)
                .build());

        // first check-in wins; do not downgrade PRESENT to LATE on repeated submission
        if (record.getId() == null) {
            record.setStatus(status);
            record.setCheckedInAt(now);
        }

        return mapper.toRecordResponse(recordRepository.save(record));
    }

    private CheckInSession loadSession(UUID sessionId) {
        return sessionRepository.findByIdWithDetails(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("CheckInSession not found: " + sessionId));
    }

    private Map<UUID, CheckInRecord> recordsByStudentId(UUID sessionId) {
        var map = new HashMap<UUID, CheckInRecord>();
        for (var r : recordRepository.findBySessionId(sessionId)) {
            map.put(r.getStudent().getId(), r);
        }
        return map;
    }

    private List<Student> loadStudents(TeacherSubjectPermission permission) {
        var groupId = permission.getGroup().getId();
        var students = permission.getAllowedSubgroup() != null
                       ? studentRepository.findByGroupIdAndSubgroupIdAndArchivedAtIsNull(
            groupId,
            permission.getAllowedSubgroup().getId()
        )
                       : studentRepository.findByGroupIdAndArchivedAtIsNull(groupId);
        return students.stream().sorted(Comparator.comparing(Student::getUsername)).toList();
    }

    private void assertLessonMatchesPermission(Lesson lesson, TeacherSubjectPermission permission) {
        if (!lesson.getSubject().getId().equals(permission.getSubject().getId())) {
            throw new IllegalArgumentException("Lesson subject does not match permission");
        }
        if (!lesson.getGroup().getId().equals(permission.getGroup().getId())) {
            throw new IllegalArgumentException("Lesson group does not match permission");
        }
        if (permission.getAllowedSubgroup() != null) {
            if (lesson.getSubgroup() == null
                || !lesson.getSubgroup().getId().equals(permission.getAllowedSubgroup().getId())) {
                throw new IllegalArgumentException("Lesson subgroup does not match permission");
            }
        }
        if (permission.getAllowedLessonType() != null
            && permission.getAllowedLessonType() != lesson.getType()) {
            throw new IllegalArgumentException("Lesson type does not match permission");
        }
    }

    private static AttendanceStatus proposedAttendanceStatus(CheckInRecordStatus status) {
        if (status == null) {
            return AttendanceStatus.ABSENT;
        }
        return switch (status) {
            case PRESENT -> AttendanceStatus.PRESENT;
            case LATE -> AttendanceStatus.LATE;
        };
    }
}
