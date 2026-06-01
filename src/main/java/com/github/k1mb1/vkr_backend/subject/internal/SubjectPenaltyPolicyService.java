package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectPenaltyPolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyPolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.PenaltyPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.PenaltyPolicyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectPenaltyPolicyService
    implements SubjectPenaltyPolicyApi {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Override
    public PenaltyPolicyResponse getPenaltyPolicy(UUID subjectId) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toPenaltyPolicyResponse(subject.getPenaltyPolicy());
    }

    @Transactional
    @Override
    public PenaltyPolicyResponse updatePenaltyPolicy(UUID subjectId, PenaltyPolicyRequest request) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.setPenaltyPolicy(toPenaltyPolicy(request));
        return subjectMapper.toPenaltyPolicyResponse(subjectRepository.save(subject).getPenaltyPolicy());
    }

    /**
     * Преобразует запрос политики в embeddable. Понижение и бонус независимы:
     * при enabled=true обязательны поля понижения, при bonusEnabled=true — поля бонуса.
     * Выключенная сторона хранит null в своих параметрах.
     */
    private PenaltyPolicy toPenaltyPolicy(PenaltyPolicyRequest request) {
        var builder = PenaltyPolicy.builder()
            .enabled(false)
            .bonusEnabled(false);

        if (Boolean.TRUE.equals(request.enabled())) {
            if (request.operation() == null
                || request.step() == null
                || request.gracePeriodLessons() == null
                || request.intervalLessons() == null
                || request.maxReductions() == null) {
                throw new IllegalArgumentException(
                    "При включённом понижении (enabled=true) обязательны поля: "
                    + "operation, step, gracePeriodLessons, intervalLessons, maxReductions"
                );
            }
            builder.enabled(true)
                .operation(request.operation())
                .step(request.step())
                .gracePeriodLessons(request.gracePeriodLessons())
                .intervalLessons(request.intervalLessons())
                .maxReductions(request.maxReductions());
        }

        if (Boolean.TRUE.equals(request.bonusEnabled())) {
            if (request.bonusOperation() == null
                || request.bonusStep() == null
                || request.bonusGracePeriodLessons() == null
                || request.bonusIntervalLessons() == null
                || request.bonusMaxIncreases() == null) {
                throw new IllegalArgumentException(
                    "При включённом бонусе (bonusEnabled=true) обязательны поля: "
                    + "bonusOperation, bonusStep, bonusGracePeriodLessons, bonusIntervalLessons, bonusMaxIncreases"
                );
            }
            builder.bonusEnabled(true)
                .bonusOperation(request.bonusOperation())
                .bonusStep(request.bonusStep())
                .bonusGracePeriodLessons(request.bonusGracePeriodLessons())
                .bonusIntervalLessons(request.bonusIntervalLessons())
                .bonusMaxIncreases(request.bonusMaxIncreases());
        }

        return builder.build();
    }
}
