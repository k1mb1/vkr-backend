package com.github.k1mb1.vkr_backend.subject.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.api.PenaltyPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.domain.PenaltyPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.PenaltyPolicyRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectPenaltyPolicyService {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @PreAuthorize("@authz.canAccessSubject(#subjectId)")
    public PenaltyPolicyResponse getPenaltyPolicy(UUID subjectId) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toPenaltyPolicyResponse(subject.getPenaltyPolicy());
    }

    @Transactional
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public PenaltyPolicyResponse updatePenaltyPolicy(UUID subjectId, PenaltyPolicyRequest request) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.setPenaltyPolicy(toPenaltyPolicy(request));
        return subjectMapper.toPenaltyPolicyResponse(
                subjectRepository.save(subject).getPenaltyPolicy());
    }

    /**
     * Собирает новую политику из запроса. Понижение и бонус независимы:
     * при enabled=true обязательны поля понижения, при bonusEnabled=true — поля бонуса.
     * Выключенная сторона получает значения по умолчанию (через {@code @Builder.Default}).
     */
    private PenaltyPolicyEntity toPenaltyPolicy(PenaltyPolicyRequest request) {
        var builder = PenaltyPolicyEntity.builder();

        if (Boolean.TRUE.equals(request.enabled())) {
            if (request.operation() == null
                    || request.step() == null
                    || request.gracePeriodLessons() == null
                    || request.intervalLessons() == null
                    || request.maxReductions() == null) {
                throw new IllegalArgumentException("При включённом понижении (enabled=true) обязательны поля: "
                        + "operation, step, gracePeriodLessons, intervalLessons, maxReductions");
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
                                + "bonusOperation, bonusStep, bonusGracePeriodLessons, bonusIntervalLessons, bonusMaxIncreases");
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
