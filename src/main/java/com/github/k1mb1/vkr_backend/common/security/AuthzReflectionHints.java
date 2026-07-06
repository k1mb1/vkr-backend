package com.github.k1mb1.vkr_backend.common.security;

import com.github.k1mb1.vkr_backend.attendance.checkin.web.filters.CheckInSessionFilter;
import com.github.k1mb1.vkr_backend.attendance.checkin.web.requests.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.web.filters.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.web.requests.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.web.requests.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.grading.web.filters.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.web.requests.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.lesson.web.filters.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.web.requests.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.CreateTeacherSubjectPermissionRequest;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

/**
 * SpEL внутри {@code @PreAuthorize} обращается к акцессорам DTO по рефлексии
 * ({@code #filter.permissionId()}, {@code #request.items()} и т.п.). В GraalVM
 * native-image рефлексия по умолчанию отключена — без явных хинтов вызов
 * падает с «Failed to evaluate expression» (см. Spring Security ref, «Method
 * Security in GraalVM Native Image»).
 *
 * <p>Здесь регистрируются все типы, чьи методы используются в security-выражениях.
 * При добавлении нового {@code @PreAuthorize}, который вызывает метод DTO — не
 * забыть дописать сюда его тип.
 */
@Configuration
@RegisterReflectionForBinding({
    LessonFilter.class,
    BulkCreateLessonsRequest.class,
    BulkScheduleLessonsRequest.class,
    GradingFilter.class,
    BulkUpsertGradesRequest.class,
    UpsertGradeRequest.class,
    CreateAssignmentsRequest.class,
    AttendanceFilter.class,
    BulkUpsertAttendanceRequest.class,
    UpsertAttendanceRequest.class,
    StartCheckInRequest.class,
    CheckInSessionFilter.class,
    CreateTeacherSubjectPermissionRequest.class,
})
public class AuthzReflectionHints {}
