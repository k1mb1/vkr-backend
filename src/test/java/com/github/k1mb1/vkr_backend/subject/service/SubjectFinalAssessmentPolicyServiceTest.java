package com.github.k1mb1.vkr_backend.subject.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.domain.SubjectEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.FinalAssessmentPolicyRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectFinalAssessmentPolicyServiceTest {

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    SubjectMapper subjectMapper;

    SubjectFinalAssessmentPolicyService service;

    final UUID subjectId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SubjectFinalAssessmentPolicyService(subjectRepository, subjectMapper);
        lenient().when(subjectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private FinalAssessmentPolicyRequest request(
            Boolean enabled, java.util.List<FinalAssessmentPolicyRequest.Band> bands) {
        return new FinalAssessmentPolicyRequest(enabled, bands, null, null, null, null, null, null, null, null);
    }

    @Test
    void getThrowsWhenSubjectMissing() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getFinalAssessmentPolicy(subjectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void disabledUpdateClearsBandsAndSucceeds() {
        var subject = SubjectEntity.builder().id(subjectId).name("S").build();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThatCode(() -> service.updateFinalAssessmentPolicy(subjectId, request(false, null)))
                .doesNotThrowAnyException();
        assertThat(subject.getFinalAssessmentPolicy().isEnabled()).isFalse();
        assertThat(subject.getFinalAssessmentPolicy().getBands()).isEmpty();
    }

    @Test
    void enabledWithoutBandsThrows() {
        var subject = SubjectEntity.builder().id(subjectId).name("S").build();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));

        assertThatThrownBy(() -> service.updateFinalAssessmentPolicy(subjectId, request(true, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("непустой bands");
    }
}
