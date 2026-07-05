package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectAttendanceHighlightPolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.AttendanceHighlightPolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendanceHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendanceHighlightPolicyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectAttendanceHighlightPolicyService implements SubjectAttendanceHighlightPolicyApi {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Override
    @PreAuthorize("@authz.canAccessSubject(#a0)")
    public AttendanceHighlightPolicyResponse getAttendanceHighlightPolicy(UUID subjectId) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toAttendanceHighlightPolicyResponse(subject.getAttendanceHighlightPolicy());
    }

    @Transactional
    @Override
    @PreAuthorize("@authz.canManageSubject(#a0)")
    public AttendanceHighlightPolicyResponse updateAttendanceHighlightPolicy(
            UUID subjectId, AttendanceHighlightPolicyRequest request) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.setAttendanceHighlightPolicy(toAttendanceHighlightPolicy(request));
        return subjectMapper.toAttendanceHighlightPolicyResponse(
                subjectRepository.save(subject).getAttendanceHighlightPolicy());
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — все поля получают
     * значения по умолчанию (через {@code @Builder.Default}).
     */
    private AttendanceHighlightPolicy toAttendanceHighlightPolicy(AttendanceHighlightPolicyRequest request) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return AttendanceHighlightPolicy.builder().build();
        }
        if (request.presentColor() == null
                || request.lateColor() == null
                || request.absentColor() == null
                || request.excusedColor() == null) {
            throw new IllegalArgumentException("При включённой подсветке посещаемости (enabled=true) обязательны: "
                    + "presentColor, lateColor, absentColor, excusedColor");
        }
        return AttendanceHighlightPolicy.builder()
                .enabled(true)
                .presentColor(request.presentColor())
                .lateColor(request.lateColor())
                .absentColor(request.absentColor())
                .excusedColor(request.excusedColor())
                .build();
    }
}
