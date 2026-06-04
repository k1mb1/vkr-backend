package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectCheckInPolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.CheckInPolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.CheckInPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.CheckInPolicyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectCheckInPolicyService
    implements SubjectCheckInPolicyApi {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Override
    public CheckInPolicyResponse getCheckInPolicy(UUID subjectId) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toCheckInPolicyResponse(subject.getCheckInPolicy());
    }

    @Transactional
    @Override
    public CheckInPolicyResponse updateCheckInPolicy(UUID subjectId, CheckInPolicyRequest request) {
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.setCheckInPolicy(toCheckInPolicy(request));
        return subjectMapper.toCheckInPolicyResponse(
            subjectRepository.save(subject).getCheckInPolicy()
        );
    }

    /**
     * Преобразует запрос в embeddable. Если enabled=false — выключенная политика без окон.
     * При enabled=true оба окна обязательны.
     */
    private CheckInPolicy toCheckInPolicy(CheckInPolicyRequest request) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return CheckInPolicy.builder().enabled(false).build();
        }
        if (request.onTimeSeconds() == null || request.lateSeconds() == null) {
            throw new IllegalArgumentException(
                "При включённой политике check-in (enabled=true) обязательны: "
                    + "onTimeSeconds, lateSeconds"
            );
        }
        return CheckInPolicy.builder()
            .enabled(true)
            .onTimeSeconds(request.onTimeSeconds())
            .lateSeconds(request.lateSeconds())
            .build();
    }
}
