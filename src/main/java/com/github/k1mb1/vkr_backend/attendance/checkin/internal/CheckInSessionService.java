package com.github.k1mb1.vkr_backend.attendance.checkin.internal;

import com.github.k1mb1.vkr_backend.attendance.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.CheckInSessionApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecord;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSession;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.filters.CheckInSessionFilter;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.responses.PublicCheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.common.util.NameMasker;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonSpecifications;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CheckInSessionService implements CheckInSessionApi {

    final CheckInSessionRepository sessionRepository;

    final CheckInRecordRepository recordRepository;

    final LessonRepository lessonRepository;

    final LessonScopeRepository lessonScopeRepository;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonStudentsApi lessonStudentsApi;

    final CheckInSessionMapper mapper;

    final AttendanceApi attendanceApi;

    private static AttendanceStatus proposedAttendanceStatus(
        CheckInRecordStatus status
    ) {
        if (status == null) {
            return AttendanceStatus.ABSENT;
        }
        return switch (status) {
            case PRESENT -> AttendanceStatus.PRESENT;
            case LATE -> AttendanceStatus.LATE;
        };
    }

    @Transactional
    @Override
    public CheckInSessionResponse start(StartCheckInRequest request) {
        var scope = lessonScopeRepository
            .findWithDetailsById(request.lessonScopeId())
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "LessonScope",
                    request.lessonScopeId()
                )
            );

        sessionRepository
            .findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(
                scope.getId()
            )
            .ifPresent(existing -> {
                throw new IllegalStateException(
                    "Active check-in session already exists for lesson scope: " +
                        scope.getId()
                );
            });

        var session = CheckInSession.builder()
            .lessonScope(scope)
            .startedAt(Instant.now())
            .onTimeSeconds(request.onTimeSeconds())
            .lateSeconds(request.lateSeconds())
            .build();

        return mapper.toResponse(
            sessionRepository.save(session),
            Instant.now()
        );
    }

    @Override
    public CheckInSessionResponse get(UUID sessionId) {
        var session = loadSession(sessionId);
        return mapper.toResponse(session, Instant.now());
    }

    @Override
    public List<CheckInSessionResponse> list(CheckInSessionFilter filter) {
        var permission = permissionRepository
            .findWithDetailsById(filter.permissionId())
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "TeacherSubjectPermission",
                    filter.permissionId()
                )
            );

        var scopeIds = resolveScopeIds(permission, filter);
        if (scopeIds.isEmpty()) {
            return List.of();
        }

        var now = Instant.now();
        return sessionRepository
            .findByLessonScopeIdInOrderByStartedAtDesc(scopeIds)
            .stream()
            .map(s -> mapper.toResponse(s, now))
            .toList();
    }

    private List<UUID> resolveScopeIds(
        TeacherSubjectPermission permission,
        CheckInSessionFilter filter
    ) {
        if (filter.lessonScopeId() != null) {
            var scope = lessonScopeRepository
                .findById(filter.lessonScopeId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "LessonScope",
                        filter.lessonScopeId()
                    )
                );
            assertSameSubject(scope.getLesson(), permission);
            assertLessonMatch(scope.getLesson(), filter.lessonId());
            return List.of(scope.getId());
        }
        if (filter.lessonId() != null) {
            var lesson = lessonRepository
                .findById(filter.lessonId())
                .orElseThrow(() ->
                    new ResourceNotFoundException("Lesson", filter.lessonId())
                );
            assertSameSubject(lesson, permission);
            return lesson.getScopes().stream().map(LessonScope::getId).toList();
        }
        return lessonRepository
            .findAll(LessonSpecifications.forPermission(permission))
            .stream()
            .flatMap(l ->
                LessonSpecifications.visibleScopes(l, permission).stream()
            )
            .map(LessonScope::getId)
            .toList();
    }

    private void assertSameSubject(
        Lesson lesson,
        TeacherSubjectPermission permission
    ) {
        if (
            !lesson.getSubject().getId().equals(permission.getSubject().getId())
        ) {
            throw new IllegalArgumentException(
                "Lesson " +
                    lesson.getId() +
                    " does not belong to subject of permission " +
                    permission.getId()
            );
        }
    }

    private void assertLessonMatch(Lesson scopeLesson, UUID requestedLessonId) {
        if (
            requestedLessonId != null &&
            !scopeLesson.getId().equals(requestedLessonId)
        ) {
            throw new IllegalArgumentException(
                "lessonScopeId belongs to lesson " +
                    scopeLesson.getId() +
                    " but lessonId=" +
                    requestedLessonId
            );
        }
    }

    @Override
    public CheckInPreviewResponse preview(UUID sessionId) {
        var session = loadSession(sessionId);
        var students = lessonStudentsApi.studentsOf(session.getLessonScope());
        var recordsByStudent = recordsByStudentId(sessionId);

        var rows = students
            .stream()
            .map(student -> {
                var record = recordsByStudent.get(student.getId());
                var checkInStatus = record != null ? record.getStatus() : null;
                var checkedInAt =
                    record != null ? record.getCheckedInAt() : null;
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

        return new CheckInPreviewResponse(
            mapper.toResponse(session, Instant.now()),
            rows
        );
    }

    @Transactional
    @Override
    public CheckInSessionResponse confirm(
        UUID sessionId,
        ConfirmCheckInRequest request
    ) {
        var session = loadSession(sessionId);
        if (session.getConfirmedAt() != null) {
            throw new IllegalStateException(
                "Session already confirmed: " + sessionId
            );
        }
        if (session.getCancelledAt() != null) {
            throw new IllegalStateException(
                "Session is cancelled: " + sessionId
            );
        }

        var students = lessonStudentsApi.studentsOf(session.getLessonScope());
        var studentIds = students
            .stream()
            .map(Student::getId)
            .collect(java.util.stream.Collectors.toSet());
        var recordsByStudent = recordsByStudentId(sessionId);

        var overridesByStudent = new HashMap<
            UUID,
            ConfirmCheckInRequest.Override
        >();
        if (request != null && request.overrides() != null) {
            for (var ov : request.overrides()) {
                if (!studentIds.contains(ov.studentId())) {
                    throw new IllegalArgumentException(
                        "Override references student not in lesson audience: " +
                            ov.studentId()
                    );
                }
                overridesByStudent.put(ov.studentId(), ov);
            }
        }

        var scopeId = session.getLessonScope().getId();
        var items = new java.util.ArrayList<UpsertAttendanceRequest>(
            students.size()
        );
        for (var student : students) {
            var override = overridesByStudent.get(student.getId());
            AttendanceStatus status;
            String comment;
            if (override != null) {
                status = override.status();
                comment = override.comment();
            } else {
                var record = recordsByStudent.get(student.getId());
                status = proposedAttendanceStatus(
                    record != null ? record.getStatus() : null
                );
                comment = null;
            }
            items.add(
                new UpsertAttendanceRequest(
                    student.getId(),
                    scopeId,
                    status,
                    comment
                )
            );
        }
        if (!items.isEmpty()) {
            attendanceApi.upsertAll(new BulkUpsertAttendanceRequest(items));
        }

        session.setConfirmedAt(Instant.now());
        return mapper.toResponse(
            sessionRepository.save(session),
            Instant.now()
        );
    }

    @Transactional
    @Override
    public CheckInSessionResponse cancel(UUID sessionId) {
        var session = loadSession(sessionId);
        if (session.getConfirmedAt() != null) {
            throw new IllegalStateException(
                "Cannot cancel a confirmed session: " + sessionId
            );
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
        var scope = session.getLessonScope();
        var lesson = scope.getLesson();
        var students = lessonStudentsApi.studentsOf(scope);
        var recordsByStudent = recordsByStudentId(sessionId);
        var now = Instant.now();

        var rows = students
            .stream()
            .map(student -> {
                var record = recordsByStudent.get(student.getId());
                return new PublicCheckInSessionResponse.Student(
                    student.getId(),
                    NameMasker.maskFullName(student.getUsername()),
                    record != null ? record.getStatus() : null,
                    record != null ? record.getCheckedInAt() : null
                );
            })
            .toList();

        return new PublicCheckInSessionResponse(
            session.getId(),
            lesson.getTopic(),
            mapper.audienceOf(scope),
            session.stateAt(now),
            session.onTimeEndsAt(),
            session.lateEndsAt(),
            now,
            rows
        );
    }

    private CheckInSession loadSession(UUID sessionId) {
        return sessionRepository
            .findWithDetailsById(sessionId)
            .orElseThrow(() ->
                new ResourceNotFoundException("CheckInSession", sessionId)
            );
    }

    private Map<UUID, CheckInRecord> recordsByStudentId(UUID sessionId) {
        var map = new HashMap<UUID, CheckInRecord>();
        for (var r : recordRepository.findBySessionId(sessionId)) {
            map.put(r.getStudent().getId(), r);
        }
        return map;
    }
}
