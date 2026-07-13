package com.github.k1mb1.vkr_backend.attendance.checkin.service;

import com.github.k1mb1.vkr_backend.attendance.api.AttendanceApi;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInRecordStatus;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionEntity;
import com.github.k1mb1.vkr_backend.attendance.checkin.domain.CheckInSessionState;
import com.github.k1mb1.vkr_backend.attendance.checkin.mapper.CheckInSessionMapper;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInRecordRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.repository.CheckInSessionRepository;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.filter.CheckInSessionFilter;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.ConfirmCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.CheckInPreviewResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.CheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.PublicCheckInSessionResponse;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.response.PublicStudentResponse;
import com.github.k1mb1.vkr_backend.attendance.domain.AttendanceStatus;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.common.exception.ConflictException;
import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.common.util.NameMasker;
import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.repository.LessonScopeRepository;
import com.github.k1mb1.vkr_backend.lesson.service.LessonResolver;
import com.github.k1mb1.vkr_backend.lesson.specification.LessonSpecifications;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermissionEntity;
import com.github.k1mb1.vkr_backend.subject.repository.TeacherSubjectPermissionRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckInSessionService {

    /** Минимальная длина запроса для поиска студента по фамилии — отсекает попытку выгрузить весь ростер пустым/коротким запросом. */
    private static final int MIN_SEARCH_QUERY_LENGTH = 2;

    final CheckInSessionRepository sessionRepository;

    final CheckInRecordRepository recordRepository;

    final LessonRepository lessonRepository;

    final LessonScopeRepository lessonScopeRepository;

    final TeacherSubjectPermissionRepository permissionRepository;

    final LessonResolver lessonResolver;

    final LessonStudentsApi lessonStudentsApi;

    final CheckInSessionMapper mapper;

    final AttendanceApi attendanceApi;

    private static AttendanceStatus proposedAttendanceStatus(@Nullable CheckInRecordStatus status) {
        if (status == null) {
            return AttendanceStatus.ABSENT;
        }
        return switch (status) {
            case PRESENT -> AttendanceStatus.PRESENT;
            case LATE -> AttendanceStatus.LATE;
        };
    }

    @Transactional
    @PreAuthorize("@authz.canAccessLessonScopes({#request.lessonScopeId()})")
    public CheckInSessionResponse start(StartCheckInRequest request) {
        var scope = lessonScopeRepository
                .findWithDetailsById(request.lessonScopeId())
                .orElseThrow(() -> new ResourceNotFoundException("LessonScope", request.lessonScopeId()));

        sessionRepository
                .findByLessonScopeIdAndConfirmedAtIsNullAndCancelledAtIsNull(scope.getId())
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Active check-in session already exists for lesson scope: " + scope.getId());
                });

        // Если у предмета задана политика check-in — единые окна для всех его сессий
        // берутся из неё, а значения из запроса игнорируются. Иначе окна берём из запроса.
        var policy = scope.getLesson().getSubject().getCheckInPolicy();
        int onTimeSeconds;
        int lateSeconds;
        if (policy.isEnabled()) {
            onTimeSeconds = policy.getOnTimeSeconds();
            lateSeconds = policy.getLateSeconds();
        } else {
            if (request.onTimeSeconds() == null || request.lateSeconds() == null) {
                throw new IllegalArgumentException(
                        "Для предмета без политики check-in укажите onTimeSeconds и lateSeconds");
            }
            onTimeSeconds = request.onTimeSeconds();
            lateSeconds = request.lateSeconds();
        }

        var session = CheckInSessionEntity.builder()
                .lessonScope(scope)
                .startedAt(Instant.now())
                .code(CheckInCodeGenerator.generate())
                .onTimeSeconds(onTimeSeconds)
                .lateSeconds(lateSeconds)
                .build();

        return mapper.toResponse(sessionRepository.save(session), Instant.now());
    }

    @PreAuthorize("@authz.canAccessCheckInSession(#sessionId)")
    public CheckInSessionResponse get(UUID sessionId) {
        var session = loadSession(sessionId);
        return mapper.toResponse(session, Instant.now());
    }

    @PreAuthorize("@authz.ownsPermission(#filter.permissionId())")
    public List<CheckInSessionResponse> list(CheckInSessionFilter filter) {
        var permission = permissionRepository
                .findWithDetailsById(filter.permissionId())
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubjectPermission", filter.permissionId()));

        var scopeIds = resolveScopeIds(permission, filter);
        if (scopeIds.isEmpty()) {
            return List.of();
        }

        var now = Instant.now();
        return sessionRepository.findByLessonScopeIdInOrderByStartedAtDesc(scopeIds).stream()
                .map(s -> mapper.toResponse(s, now))
                .toList();
    }

    private List<UUID> resolveScopeIds(TeacherSubjectPermissionEntity permission, CheckInSessionFilter filter) {
        if (filter.lessonScopeId() != null) {
            var scope = lessonScopeRepository
                    .findById(filter.lessonScopeId())
                    .orElseThrow(() -> new ResourceNotFoundException("LessonScope", filter.lessonScopeId()));
            lessonResolver.assertSameSubject(scope.getLesson(), permission);
            lessonResolver.assertLessonMatch(scope.getLesson(), filter.lessonId());
            return List.of(scope.getId());
        }
        if (filter.lessonId() != null) {
            var lesson = lessonRepository
                    .findById(filter.lessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson", filter.lessonId()));
            lessonResolver.assertSameSubject(lesson, permission);
            return lesson.getScopes().stream().map(LessonScopeEntity::getId).toList();
        }
        return lessonRepository.findAllWithDetails(LessonSpecifications.forPermission(permission)).stream()
                .flatMap(l -> LessonSpecifications.visibleScopes(l, permission).stream())
                .map(LessonScopeEntity::getId)
                .toList();
    }

    @PreAuthorize("@authz.canAccessCheckInSession(#sessionId)")
    public CheckInPreviewResponse preview(UUID sessionId) {
        var session = loadSession(sessionId);
        var students = lessonStudentsApi.studentsOf(session.getLessonScope());
        var recordsByStudent = recordsByStudentId(sessionId);

        var rows = students.stream()
                .map(student -> {
                    var record = recordsByStudent.get(student.getId());
                    var checkInStatus = record != null ? record.getStatus() : null;
                    var checkedInAt = record != null ? record.getCheckedInAt() : null;
                    var proposed = proposedAttendanceStatus(checkInStatus);
                    return CheckInPreviewResponse.Row.builder()
                            .studentId(student.getId())
                            .username(student.getUsername())
                            .checkInStatus(checkInStatus)
                            .checkedInAt(checkedInAt)
                            .proposedStatus(proposed)
                            .build();
                })
                .toList();

        return CheckInPreviewResponse.builder()
                .session(mapper.toResponse(session, Instant.now()))
                .rows(rows)
                .build();
    }

    @Transactional
    @PreAuthorize("@authz.canAccessCheckInSession(#sessionId)")
    public CheckInSessionResponse confirm(UUID sessionId, ConfirmCheckInRequest request) {
        var session = loadSession(sessionId);
        if (session.getConfirmedAt() != null) {
            throw new ConflictException("Session already confirmed: " + sessionId);
        }
        if (session.getCancelledAt() != null) {
            throw new ConflictException("Session is cancelled: " + sessionId);
        }

        var students = lessonStudentsApi.studentsOf(session.getLessonScope());
        var studentIds = students.stream().map(StudentEntity::getId).collect(java.util.stream.Collectors.toSet());
        var recordsByStudent = recordsByStudentId(sessionId);

        var overridesByStudent = new HashMap<UUID, ConfirmCheckInRequest.StudentOverride>();
        if (request != null && request.overrides() != null) {
            for (var ov : request.overrides()) {
                if (!studentIds.contains(ov.studentId())) {
                    throw new IllegalArgumentException(
                            "Override references student not in lesson audience: " + ov.studentId());
                }
                overridesByStudent.put(ov.studentId(), ov);
            }
        }

        var scopeId = session.getLessonScope().getId();
        var items = new java.util.ArrayList<UpsertAttendanceRequest>(students.size());
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
            items.add(UpsertAttendanceRequest.builder()
                    .studentId(student.getId())
                    .lessonScopeId(scopeId)
                    .status(status)
                    .comment(comment)
                    .build());
        }
        if (!items.isEmpty()) {
            attendanceApi.upsertAll(new BulkUpsertAttendanceRequest(items));
        }

        session.setConfirmedAt(Instant.now());
        return mapper.toResponse(sessionRepository.save(session), Instant.now());
    }

    @Transactional
    @PreAuthorize("@authz.canAccessCheckInSession(#sessionId)")
    public CheckInSessionResponse cancel(UUID sessionId) {
        var session = loadSession(sessionId);
        if (session.getConfirmedAt() != null) {
            throw new ConflictException("Cannot cancel a confirmed session: " + sessionId);
        }
        if (session.getCancelledAt() == null) {
            session.setCancelledAt(Instant.now());
            sessionRepository.save(session);
        }
        return mapper.toResponse(session, Instant.now());
    }

    public PublicCheckInSessionResponse getPublic(UUID sessionId) {
        var session = loadSession(sessionId);
        var scope = session.getLessonScope();
        var lesson = scope.getLesson();
        var now = Instant.now();

        // Намеренно не отдаём ростер группы: чтобы отметиться, студент ищет себя
        // по фамилии через searchStudents(...). Так список группы и статусы посещаемости
        // не раскрываются всем по ссылке и не скрейпятся одним запросом.
        return PublicCheckInSessionResponse.builder()
                .id(session.getId())
                .lessonTopic(lesson.getTopic())
                .audience(mapper.audienceOf(scope))
                .state(session.stateAt(now))
                .onTimeEndsAt(session.onTimeEndsAt())
                .lateEndsAt(session.lateEndsAt())
                .serverNow(now)
                .build();
    }

    public void verifyCode(UUID sessionId, String code) {
        var session = loadSession(sessionId);
        requireOpen(session);
        if (!CheckInCodes.matches(session.getCode(), code)) {
            throw new IllegalArgumentException("Неверный код сессии");
        }
    }

    public List<PublicStudentResponse> searchStudents(UUID sessionId, String code, String query) {
        var session = loadSession(sessionId);

        // Доступ к списку — только за кодом аудитории: сверяем его до любого поиска.
        if (!CheckInCodes.matches(session.getCode(), code)) {
            throw new IllegalArgumentException("Неверный код сессии");
        }

        // Поиск работает только пока сессия открыта (основное окно или окно опоздавших).
        // После закрытия/подтверждения/отмены протёкший QR перестаёт отдавать совпадения.
        var state = session.stateAt(Instant.now());
        if (state != CheckInSessionState.OPEN && state != CheckInSessionState.LATE_WINDOW) {
            return List.of();
        }

        var normalized = query == null ? "" : query.trim();
        if (normalized.length() < MIN_SEARCH_QUERY_LENGTH) {
            return List.of();
        }
        var needle = normalized.toLowerCase(Locale.ROOT);

        var matches = lessonStudentsApi.studentsOf(session.getLessonScope()).stream()
                .filter(student -> student.getUsername() != null
                        && student.getUsername().toLowerCase(Locale.ROOT).contains(needle))
                .toList();

        // Отдаём результат только при однозначном совпадении —
        // чтобы случайный запрос не показал чужие ФИО.
        if (matches.size() != 1) {
            return List.of();
        }

        var student = matches.get(0);
        return List.of(PublicStudentResponse.builder()
                .id(student.getId())
                .username(NameMasker.maskFullName(student.getUsername()))
                .build());
    }

    private CheckInSessionEntity loadSession(UUID sessionId) {
        return sessionRepository
                .findWithDetailsById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("CheckInSession", sessionId));
    }

    private void requireOpen(CheckInSessionEntity session) {
        var state = session.stateAt(Instant.now());
        if (state != CheckInSessionState.OPEN && state != CheckInSessionState.LATE_WINDOW) {
            throw new ConflictException("Check-in is closed for session: " + session.getId());
        }
    }

    private Map<UUID, CheckInRecordEntity> recordsByStudentId(UUID sessionId) {
        var map = new HashMap<UUID, CheckInRecordEntity>();
        for (var r : recordRepository.findBySessionId(sessionId)) {
            map.put(r.getStudent().getId(), r);
        }
        return map;
    }
}
