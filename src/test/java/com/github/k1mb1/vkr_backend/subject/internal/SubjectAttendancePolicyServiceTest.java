package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.domain.AttendancePolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendancePolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectAttendancePolicyServiceTest {

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    SubjectMapper subjectMapper;

    @InjectMocks
    SubjectAttendancePolicyService service;

    private final UUID subjectId = UUID.randomUUID();

    // ------------------------------------------------------------------ getAttendancePolicy

    @Test
    void getAttendancePolicyThrowsWhenSubjectMissing() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAttendancePolicy(subjectId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(subjectId.toString());
    }

    @Test
    void getAttendancePolicyDelegatesToMapper() {
        var subject = new Subject();
        var expectedResponse = new AttendancePolicyResponse(false,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectMapper.toAttendancePolicyResponse(any())).thenReturn(expectedResponse);

        var result = service.getAttendancePolicy(subjectId);

        assertThat(result).isSameAs(expectedResponse);
    }

    // ------------------------------------------------------------------ updateAttendancePolicy

    @Test
    void updateAttendancePolicyThrowsWhenSubjectMissing() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAttendancePolicy(subjectId, disabledRequest()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAttendancePolicyWithEnabledAndMissingFieldsThrows() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(new Subject()));

        var badRequest = new AttendancePolicyRequest(true,
            BigDecimal.ONE,
            null,     // pointsLate missing
            BigDecimal.ONE,
            BigDecimal.ONE);

        assertThatThrownBy(() -> service.updateAttendancePolicy(subjectId, badRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("enabled=true");
    }

    @Test
    void updateAttendancePolicyDisabledStoresDisabledPolicy() {
        var subject = new Subject();
        var disabledResponse = new AttendancePolicyResponse(false,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);
        when(subjectMapper.toAttendancePolicyResponse(any())).thenReturn(disabledResponse);

        var result = service.updateAttendancePolicy(subjectId, disabledRequest());

        assertThat(subject.getAttendancePolicy().isEnabled()).isFalse();
        assertThat(result).isSameAs(disabledResponse);
    }

    @Test
    void updateAttendancePolicyEnabledWithAllFieldsStoresPolicy() {
        var subject = new Subject();
        var enabledResponse = new AttendancePolicyResponse(true,
            new BigDecimal("1.0"), new BigDecimal("0.5"),
            new BigDecimal("-1.0"), new BigDecimal("0.0"));

        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);
        when(subjectMapper.toAttendancePolicyResponse(any())).thenReturn(enabledResponse);

        var request = new AttendancePolicyRequest(true,
            new BigDecimal("1.0"),
            new BigDecimal("0.5"),
            new BigDecimal("-1.0"),
            new BigDecimal("0.0"));

        var result = service.updateAttendancePolicy(subjectId, request);

        var policy = subject.getAttendancePolicy();
        assertThat(policy.isEnabled()).isTrue();
        assertThat(policy.getPointsPresent()).isEqualByComparingTo("1.0");
        assertThat(policy.getPointsLate()).isEqualByComparingTo("0.5");
        assertThat(policy.getPointsAbsent()).isEqualByComparingTo("-1.0");
        assertThat(policy.getPointsExcused()).isEqualByComparingTo("0.0");
        assertThat(result).isSameAs(enabledResponse);
    }

    private AttendancePolicyRequest disabledRequest() {
        return new AttendancePolicyRequest(false, null, null, null, null);
    }
}
