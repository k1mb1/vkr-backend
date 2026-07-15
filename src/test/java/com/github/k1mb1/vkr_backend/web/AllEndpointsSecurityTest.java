package com.github.k1mb1.vkr_backend.web;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.k1mb1.vkr_backend.group.service.GroupService;
import com.github.k1mb1.vkr_backend.group.service.SubgroupService;
import com.github.k1mb1.vkr_backend.journal.checkin.service.CheckInRecordService;
import com.github.k1mb1.vkr_backend.journal.checkin.service.CheckInSessionService;
import com.github.k1mb1.vkr_backend.journal.service.AttendanceService;
import com.github.k1mb1.vkr_backend.journal.service.GradingService;
import com.github.k1mb1.vkr_backend.lesson.service.LessonScopeService;
import com.github.k1mb1.vkr_backend.lesson.service.LessonService;
import com.github.k1mb1.vkr_backend.subject.service.SubjectAttendanceHighlightPolicyService;
import com.github.k1mb1.vkr_backend.subject.service.SubjectAttendancePolicyService;
import com.github.k1mb1.vkr_backend.subject.service.SubjectCheckInPolicyService;
import com.github.k1mb1.vkr_backend.subject.service.SubjectFinalAssessmentPolicyService;
import com.github.k1mb1.vkr_backend.subject.service.SubjectGradingHighlightPolicyService;
import com.github.k1mb1.vkr_backend.subject.service.SubjectPenaltyPolicyService;
import com.github.k1mb1.vkr_backend.subject.service.SubjectService;
import com.github.k1mb1.vkr_backend.teacher.service.TeacherService;
import com.github.k1mb1.vkr_backend.teacher.service.TeacherSubjectPermissionService;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * The security contract for the whole API in one slice: every business endpoint
 * rejects an unauthenticated request with 401, and the public check-in surface is
 * reachable without a token. Loads all controllers with their services mocked, so
 * a new endpoint that forgets to sit behind the authenticated filter is caught here.
 */
@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
// All controllers load in one slice, so each needs a mocked service — the field
// count is inherent to a whole-API security check, not a design smell.
@SuppressWarnings("PMD.TooManyFields")
class AllEndpointsSecurityTest extends AbstractControllerTest {

    private static final String SUBJECT = "/subjects/11111111-1111-1111-1111-111111111111";

    @MockitoBean
    GroupService groupService;

    @MockitoBean
    SubgroupService subgroupService;

    @MockitoBean
    CheckInSessionService checkInSessionService;

    @MockitoBean
    CheckInRecordService checkInRecordService;

    @MockitoBean
    GradingService gradingService;

    @MockitoBean
    AttendanceService attendanceService;

    @MockitoBean
    LessonService lessonService;

    @MockitoBean
    LessonScopeService lessonScopeService;

    @MockitoBean
    SubjectService subjectService;

    @MockitoBean
    SubjectAttendanceHighlightPolicyService subjectAttendanceHighlightPolicyService;

    @MockitoBean
    SubjectAttendancePolicyService subjectAttendancePolicyService;

    @MockitoBean
    SubjectCheckInPolicyService subjectCheckInPolicyService;

    @MockitoBean
    SubjectFinalAssessmentPolicyService subjectFinalAssessmentPolicyService;

    @MockitoBean
    SubjectGradingHighlightPolicyService subjectGradingHighlightPolicyService;

    @MockitoBean
    SubjectPenaltyPolicyService subjectPenaltyPolicyService;

    @MockitoBean
    TeacherService teacherService;

    @MockitoBean
    TeacherSubjectPermissionService teacherSubjectPermissionService;

    static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                arguments("GET", "/api/groups"),
                arguments("GET", "/api/subgroups"),
                arguments("GET", "/api/check-in-sessions"),
                arguments("GET", "/api/assignments"),
                arguments("GET", "/api/attendances"),
                arguments("GET", "/api/grades"),
                arguments("GET", "/api/results"),
                arguments("GET", "/api/lessons"),
                arguments("POST", "/api/lessons/11111111-1111-1111-1111-111111111111/scopes"),
                arguments("GET", "/api/attendance-policy" + SUBJECT),
                arguments("GET", "/api/check-in-policy" + SUBJECT),
                arguments("GET", "/api/penalty-policy" + SUBJECT),
                arguments("GET", "/api/attendance-highlight-policy" + SUBJECT),
                arguments("GET", "/api/final-assessment-policy" + SUBJECT),
                arguments("GET", "/api/grading-highlight-policy" + SUBJECT),
                arguments("GET", "/api/teacher-subject-permissions"),
                arguments("GET", "/api/teachers"),
                arguments("GET", "/api/subjects"));
    }

    @ParameterizedTest(name = "{0} {1} requires authentication")
    @MethodSource("protectedEndpoints")
    void protectedEndpointRejectsAnonymous(String method, String path) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path)).andExpect(status().isUnauthorized());
    }

    @org.junit.jupiter.api.Test
    void publicCheckInEndpointIsReachableWithoutToken() throws Exception {
        mvc.perform(get("/api/check-in-sessions/public/{id}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk());
    }
}
