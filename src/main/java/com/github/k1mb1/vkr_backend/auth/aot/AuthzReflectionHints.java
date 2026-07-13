package com.github.k1mb1.vkr_backend.auth.aot;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * SpEL внутри {@code @PreAuthorize} обращается к акцессорам DTO по рефлексии
 * ({@code #filter.permissionId()}, {@code #request.items()} и т.п.). В GraalVM
 * native-image рефлексия по умолчанию отключена — без явных хинтов вызов
 * падает с «Failed to evaluate expression» (см. Spring Security ref, «Method
 * Security in GraalVM Native Image»).
 *
 * <p>Типы перечислены строковыми {@link TypeReference}, а не class-литералами:
 * auth — лист модульного графа и не может зависеть от DTO бизнес-модулей.
 * При добавлении нового {@code @PreAuthorize}, который вызывает метод DTO, — не
 * забыть дописать сюда его полное имя (и имя вложенного типа элементов, если
 * выражение ходит по элементам списка).
 */
@Configuration
@ImportRuntimeHints(AuthzReflectionHints.SecurityExpressionDtoHints.class)
public class AuthzReflectionHints {

    static class SecurityExpressionDtoHints implements RuntimeHintsRegistrar {

        private static final String BASE = "com.github.k1mb1.vkr_backend.";

        private static final List<String> SECURITY_EXPRESSION_TYPES = List.of(
                BASE + "lesson.service.dto.filter.LessonFilter",
                BASE + "lesson.service.dto.request.BulkCreateLessonsRequest",
                BASE + "lesson.service.dto.request.BulkScheduleLessonsRequest",
                BASE + "lesson.service.dto.request.BulkScheduleLessonsRequest$Item",
                BASE + "grading.service.dto.filter.GradingFilter",
                BASE + "grading.service.dto.request.BulkUpsertGradesRequest",
                BASE + "grading.service.dto.request.UpsertGradeRequest",
                BASE + "grading.service.dto.request.CreateAssignmentsRequest",
                BASE + "grading.service.dto.request.CreateAssignmentsRequest$Item",
                BASE + "attendance.service.dto.filter.AttendanceFilter",
                BASE + "attendance.service.dto.request.BulkUpsertAttendanceRequest",
                BASE + "attendance.service.dto.request.UpsertAttendanceRequest",
                BASE + "attendance.checkin.service.dto.request.StartCheckInRequest",
                BASE + "attendance.checkin.service.dto.filter.CheckInSessionFilter",
                BASE + "subject.service.dto.request.CreateTeacherSubjectPermissionRequest");

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            for (String type : SECURITY_EXPRESSION_TYPES) {
                hints.reflection()
                        .registerType(
                                TypeReference.of(type),
                                MemberCategory.INVOKE_PUBLIC_METHODS,
                                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            }
        }
    }
}
