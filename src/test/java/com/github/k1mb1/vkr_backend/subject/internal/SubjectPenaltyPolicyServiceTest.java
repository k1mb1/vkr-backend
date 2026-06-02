package com.github.k1mb1.vkr_backend.subject.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.domain.BonusOperation;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyOperation;
import com.github.k1mb1.vkr_backend.subject.domain.Subject;
import com.github.k1mb1.vkr_backend.subject.web.requests.PenaltyPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectPenaltyPolicyServiceTest {

    @Mock
    SubjectRepository subjectRepository;

    @Mock
    SubjectMapper subjectMapper;

    @InjectMocks
    SubjectPenaltyPolicyService service;

    private final UUID subjectId = UUID.randomUUID();

    @Test
    void getPenaltyPolicyThrowsWhenSubjectMissing() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPenaltyPolicy(subjectId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(subjectId.toString());
    }

    @Test
    void updateThrowsWhenSubjectMissing() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePenaltyPolicy(subjectId, allDisabled()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void bothDisabledStoresNullParameters() {
        var subject = subjectFound();

        service.updatePenaltyPolicy(subjectId, allDisabled());

        var policy = subject.getPenaltyPolicy();
        assertThat(policy.isEnabled()).isFalse();
        assertThat(policy.isBonusEnabled()).isFalse();
        assertThat(policy.getOperation()).isNull();
        assertThat(policy.getStep()).isNull();
        assertThat(policy.getBonusOperation()).isNull();
        assertThat(policy.getBonusStep()).isNull();
    }

    @Test
    void enabledWithCompleteFieldsStoresPenaltyParameters() {
        var subject = subjectFound();

        var request = new PenaltyPolicyRequest(
            true,
            PenaltyOperation.SUBTRACT,
            new BigDecimal("0.5"),
            2,
            1,
            3,
            false,
            null, null, null, null, null
        );

        service.updatePenaltyPolicy(subjectId, request);

        var policy = subject.getPenaltyPolicy();
        assertThat(policy.isEnabled()).isTrue();
        assertThat(policy.getOperation()).isEqualTo(PenaltyOperation.SUBTRACT);
        assertThat(policy.getStep()).isEqualByComparingTo("0.5");
        assertThat(policy.getGracePeriodLessons()).isEqualTo(2);
        assertThat(policy.getIntervalLessons()).isEqualTo(1);
        assertThat(policy.getMaxReductions()).isEqualTo(3);
        assertThat(policy.isBonusEnabled()).isFalse();
    }

    @Test
    void bonusEnabledWithCompleteFieldsStoresBonusParameters() {
        var subject = subjectFound();

        var request = new PenaltyPolicyRequest(
            false,
            null, null, null, null, null,
            true,
            BonusOperation.ADD,
            new BigDecimal("1.5"),
            1,
            2,
            4
        );

        service.updatePenaltyPolicy(subjectId, request);

        var policy = subject.getPenaltyPolicy();
        assertThat(policy.isBonusEnabled()).isTrue();
        assertThat(policy.getBonusOperation()).isEqualTo(BonusOperation.ADD);
        assertThat(policy.getBonusStep()).isEqualByComparingTo("1.5");
        assertThat(policy.getBonusGracePeriodLessons()).isEqualTo(1);
        assertThat(policy.getBonusIntervalLessons()).isEqualTo(2);
        assertThat(policy.getBonusMaxIncreases()).isEqualTo(4);
        assertThat(policy.isEnabled()).isFalse();
    }

    @Test
    void enabledWithMissingFieldsIsRejected() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(new Subject()));

        var request = new PenaltyPolicyRequest(
            true,
            PenaltyOperation.SUBTRACT,
            null, // step missing
            2, 1, 3,
            false,
            null, null, null, null, null
        );

        assertThatThrownBy(() -> service.updatePenaltyPolicy(subjectId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("понижени");
    }

    @Test
    void bonusEnabledWithMissingFieldsIsRejected() {
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(new Subject()));

        var request = new PenaltyPolicyRequest(
            false,
            null, null, null, null, null,
            true,
            BonusOperation.ADD,
            new BigDecimal("1.1"),
            1,
            null, // bonusIntervalLessons missing
            4
        );

        assertThatThrownBy(() -> service.updatePenaltyPolicy(subjectId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("бонус");
    }

    private Subject subjectFound() {
        var subject = new Subject();
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(subjectRepository.save(subject)).thenReturn(subject);
        when(subjectMapper.toPenaltyPolicyResponse(any()))
            .thenReturn(
                new PenaltyPolicyResponse(
                    false, null, null, null, null, null,
                    false, null, null, null, null, null
                )
            );
        return subject;
    }

    private PenaltyPolicyRequest allDisabled() {
        return new PenaltyPolicyRequest(
            false,
            null, null, null, null, null,
            false,
            null, null, null, null, null
        );
    }
}
