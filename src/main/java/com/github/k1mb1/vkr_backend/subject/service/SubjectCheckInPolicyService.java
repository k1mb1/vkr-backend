package com.github.k1mb1.vkr_backend.subject.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.CheckInPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.CheckInPolicyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectCheckInPolicyService {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @PreAuthorize("@authz.canAccessSubject(#subjectId)")
    public CheckInPolicyResponse getCheckInPolicy(UUID subjectId) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toCheckInPolicyResponse(subject.getCheckInPolicy());
    }

    @Transactional
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public CheckInPolicyResponse updateCheckInPolicy(UUID subjectId, CheckInPolicyRequest request) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.setCheckInPolicy(toCheckInPolicy(request));
        return subjectMapper.toCheckInPolicyResponse(
                subjectRepository.save(subject).getCheckInPolicy());
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — все поля получают
     * значения по умолчанию (через {@code @Builder.Default}).
     */
    private CheckInPolicyEntity toCheckInPolicy(CheckInPolicyRequest request) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return CheckInPolicyEntity.builder().build();
        }
        if (request.onTimeSeconds() == null || request.lateSeconds() == null) {
            throw new IllegalArgumentException(
                    "При включённой политике check-in (enabled=true) обязательны: " + "onTimeSeconds, lateSeconds");
        }
        return CheckInPolicyEntity.builder()
                .enabled(true)
                .onTimeSeconds(request.onTimeSeconds())
                .lateSeconds(request.lateSeconds())
                .build();
    }
}
