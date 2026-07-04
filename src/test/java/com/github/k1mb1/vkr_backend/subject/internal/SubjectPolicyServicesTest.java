package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendanceHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendancePolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.CheckInPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.GradingHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.requests.PenaltyPolicyRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Общие проверки policy-сервисов предмета: 404 при отсутствии предмета,
 * успешное «выключенное» обновление и валидация обязательных полей при enabled=true.
 */
@ExtendWith(MockitoExtension.class)
class SubjectPolicyServicesTest {

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    SubjectMapper subjectMapper;

    final UUID subjectId = UUID.randomUUID();

    @BeforeEach
    void stubSubject() {
        lenient()
                .when(subjectRepository.findById(subjectId))
                .thenReturn(
                        Optional.of(Subject.builder().id(subjectId).name("S").build()));
        lenient().when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void subjectMissing() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());
    }

    // ---- Penalty ----

    @Test
    void penaltyGetThrowsWhenSubjectMissing() {
        subjectMissing();
        var service = new SubjectPenaltyPolicyService(subjectRepository, subjectMapper);
        assertThatThrownBy(() -> service.getPenaltyPolicy(subjectId)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void penaltyUpdateDisabledSucceeds() {
        var service = new SubjectPenaltyPolicyService(subjectRepository, subjectMapper);
        var request =
                new PenaltyPolicyRequest(false, null, null, null, null, null, false, null, null, null, null, null);
        assertThatCode(() -> service.updatePenaltyPolicy(subjectId, request)).doesNotThrowAnyException();
        verify(subjectRepository).save(any());
    }

    @Test
    void penaltyUpdateEnabledWithoutRequiredFieldsThrows() {
        var service = new SubjectPenaltyPolicyService(subjectRepository, subjectMapper);
        var request = new PenaltyPolicyRequest(true, null, null, null, null, null, false, null, null, null, null, null);
        assertThatThrownBy(() -> service.updatePenaltyPolicy(subjectId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void penaltyUpdateBonusEnabledWithoutRequiredFieldsThrows() {
        var service = new SubjectPenaltyPolicyService(subjectRepository, subjectMapper);
        var request = new PenaltyPolicyRequest(false, null, null, null, null, null, true, null, null, null, null, null);
        assertThatThrownBy(() -> service.updatePenaltyPolicy(subjectId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- Attendance ----

    @Test
    void attendanceUpdateDisabledSucceeds() {
        var service = new SubjectAttendancePolicyService(subjectRepository, subjectMapper);
        assertThatCode(() -> service.updateAttendancePolicy(
                        subjectId, new AttendancePolicyRequest(false, null, null, null, null)))
                .doesNotThrowAnyException();
    }

    @Test
    void attendanceUpdateEnabledWithoutPointsThrows() {
        var service = new SubjectAttendancePolicyService(subjectRepository, subjectMapper);
        assertThatThrownBy(() -> service.updateAttendancePolicy(
                        subjectId, new AttendancePolicyRequest(true, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- CheckIn ----

    @Test
    void checkInUpdateDisabledSucceeds() {
        var service = new SubjectCheckInPolicyService(subjectRepository, subjectMapper);
        assertThatCode(() -> service.updateCheckInPolicy(subjectId, new CheckInPolicyRequest(false, null, null)))
                .doesNotThrowAnyException();
    }

    @Test
    void checkInUpdateEnabledWithoutWindowsThrows() {
        var service = new SubjectCheckInPolicyService(subjectRepository, subjectMapper);
        assertThatThrownBy(() -> service.updateCheckInPolicy(subjectId, new CheckInPolicyRequest(true, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- Attendance highlight ----

    @Test
    void attendanceHighlightUpdateEnabledWithoutColorsThrows() {
        var service = new SubjectAttendanceHighlightPolicyService(subjectRepository, subjectMapper);
        assertThatThrownBy(() -> service.updateAttendanceHighlightPolicy(
                        subjectId, new AttendanceHighlightPolicyRequest(true, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void attendanceHighlightUpdateDisabledSucceeds() {
        var service = new SubjectAttendanceHighlightPolicyService(subjectRepository, subjectMapper);
        assertThatCode(() -> service.updateAttendanceHighlightPolicy(
                        subjectId, new AttendanceHighlightPolicyRequest(false, null, null, null, null)))
                .doesNotThrowAnyException();
    }

    // ---- Grading highlight ----

    @Test
    void gradingHighlightUpdateEnabledWithoutColorsThrows() {
        var service = new SubjectGradingHighlightPolicyService(subjectRepository, subjectMapper);
        assertThatThrownBy(() -> service.updateGradingHighlightPolicy(
                        subjectId, new GradingHighlightPolicyRequest(true, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gradingHighlightUpdateDisabledSucceeds() {
        var service = new SubjectGradingHighlightPolicyService(subjectRepository, subjectMapper);
        assertThatCode(() -> service.updateGradingHighlightPolicy(
                        subjectId, new GradingHighlightPolicyRequest(false, null, null, null, null)))
                .doesNotThrowAnyException();
    }
}
