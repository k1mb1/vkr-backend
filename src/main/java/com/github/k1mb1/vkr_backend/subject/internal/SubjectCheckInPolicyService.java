package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectCheckInPolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.CheckInPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.CheckInPolicyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectCheckInPolicyService implements SubjectCheckInPolicyApi {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Override
    @PreAuthorize("@authz.canAccessSubject(#subjectId)")
    public CheckInPolicyResponse getCheckInPolicy(UUID subjectId) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        return subjectMapper.toCheckInPolicyResponse(
            subject.getCheckInPolicy()
        );
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public CheckInPolicyResponse updateCheckInPolicy(
        UUID subjectId,
        CheckInPolicyRequest request
    ) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        subject.setCheckInPolicy(toCheckInPolicy(request));
        return subjectMapper.toCheckInPolicyResponse(
            subjectRepository.save(subject).getCheckInPolicy()
        );
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — все поля получают
     * значения по умолчанию (через {@code @Builder.Default}).
     */
    private CheckInPolicy toCheckInPolicy(CheckInPolicyRequest request) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return CheckInPolicy.builder().build();
        }
        if (request.onTimeSeconds() == null || request.lateSeconds() == null) {
            throw new IllegalArgumentException(
                "При включённой политике check-in (enabled=true) обязательны: " +
                    "onTimeSeconds, lateSeconds"
            );
        }
        return CheckInPolicy.builder()
            .enabled(true)
            .onTimeSeconds(request.onTimeSeconds())
            .lateSeconds(request.lateSeconds())
            .build();
    }
}
