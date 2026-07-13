package com.github.k1mb1.vkr_backend.auth.aot;

import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.filter.CheckInSessionFilter;
import com.github.k1mb1.vkr_backend.attendance.checkin.service.dto.request.StartCheckInRequest;
import com.github.k1mb1.vkr_backend.attendance.service.dto.filter.AttendanceFilter;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.BulkUpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.attendance.service.dto.request.UpsertAttendanceRequest;
import com.github.k1mb1.vkr_backend.grading.service.dto.filter.GradingFilter;
import com.github.k1mb1.vkr_backend.grading.service.dto.request.BulkUpsertGradesRequest;
import com.github.k1mb1.vkr_backend.grading.service.dto.request.CreateAssignmentsRequest;
import com.github.k1mb1.vkr_backend.grading.service.dto.request.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.filter.LessonFilter;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.BulkCreateLessonsRequest;
import com.github.k1mb1.vkr_backend.lesson.service.dto.request.BulkScheduleLessonsRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CreateTeacherSubjectPermissionRequest;
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
