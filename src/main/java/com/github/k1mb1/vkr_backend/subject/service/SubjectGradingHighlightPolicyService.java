package com.github.k1mb1.vkr_backend.subject.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.domain.GradingHighlightPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.GradingHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.GradingHighlightPolicyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectGradingHighlightPolicyService {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @PreAuthorize("@authz.canAccessSubject(#subjectId)")
    public GradingHighlightPolicyResponse getGradingHighlightPolicy(UUID subjectId) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toGradingHighlightPolicyResponse(subject.getGradingHighlightPolicy());
    }

    @Transactional
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public GradingHighlightPolicyResponse updateGradingHighlightPolicy(
            UUID subjectId, GradingHighlightPolicyRequest request) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.setGradingHighlightPolicy(toGradingHighlightPolicy(request));
        return subjectMapper.toGradingHighlightPolicyResponse(
                subjectRepository.save(subject).getGradingHighlightPolicy());
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — все поля получают
     * значения по умолчанию (через {@code @Builder.Default}).
     */
    private GradingHighlightPolicyEntity toGradingHighlightPolicy(GradingHighlightPolicyRequest request) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return GradingHighlightPolicyEntity.builder().build();
        }
        if (request.assignmentColor() == null
                || request.fullColor() == null
                || request.partialLowColor() == null
                || request.partialHighColor() == null) {
            throw new IllegalArgumentException("При включённой подсветке (enabled=true) обязательны: "
                    + "assignmentColor, fullColor, partialLowColor, partialHighColor");
        }
        return GradingHighlightPolicyEntity.builder()
                .enabled(true)
                .assignmentColor(request.assignmentColor())
                .fullColor(request.fullColor())
                .partialLowColor(request.partialLowColor())
                .partialHighColor(request.partialHighColor())
                .build();
    }
}
