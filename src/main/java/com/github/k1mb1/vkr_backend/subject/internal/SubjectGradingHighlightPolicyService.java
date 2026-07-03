package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectGradingHighlightPolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.GradingHighlightPolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.GradingHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.GradingHighlightPolicyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectGradingHighlightPolicyService
    implements SubjectGradingHighlightPolicyApi
{

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Override
    @PreAuthorize("@authz.canAccessSubject(#subjectId)")
    public GradingHighlightPolicyResponse getGradingHighlightPolicy(
        UUID subjectId
    ) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        return subjectMapper.toGradingHighlightPolicyResponse(
            subject.getGradingHighlightPolicy()
        );
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public GradingHighlightPolicyResponse updateGradingHighlightPolicy(
        UUID subjectId,
        GradingHighlightPolicyRequest request
    ) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        subject.setGradingHighlightPolicy(toGradingHighlightPolicy(request));
        return subjectMapper.toGradingHighlightPolicyResponse(
            subjectRepository.save(subject).getGradingHighlightPolicy()
        );
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — все поля получают
     * значения по умолчанию (через {@code @Builder.Default}).
     */
    private GradingHighlightPolicy toGradingHighlightPolicy(
        GradingHighlightPolicyRequest request
    ) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return GradingHighlightPolicy.builder().build();
        }
        if (
            request.assignmentColor() == null ||
            request.fullColor() == null ||
            request.partialLowColor() == null ||
            request.partialHighColor() == null
        ) {
            throw new IllegalArgumentException(
                "При включённой подсветке (enabled=true) обязательны: " +
                    "assignmentColor, fullColor, partialLowColor, partialHighColor"
            );
        }
        return GradingHighlightPolicy.builder()
            .enabled(true)
            .assignmentColor(request.assignmentColor())
            .fullColor(request.fullColor())
            .partialLowColor(request.partialLowColor())
            .partialHighColor(request.partialHighColor())
            .build();
    }
}
