package com.github.k1mb1.vkr_backend.subject.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.api.AttendanceHighlightPolicyResponse;
import com.github.k1mb1.vkr_backend.subject.domain.AttendanceHighlightPolicyEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.AttendanceHighlightPolicyRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectAttendanceHighlightPolicyService {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @PreAuthorize("@authz.canAccessSubject(#subjectId)")
    public AttendanceHighlightPolicyResponse getAttendanceHighlightPolicy(UUID subjectId) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toAttendanceHighlightPolicyResponse(subject.getAttendanceHighlightPolicy());
    }

    @Transactional
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
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
    private AttendanceHighlightPolicyEntity toAttendanceHighlightPolicy(AttendanceHighlightPolicyRequest request) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return AttendanceHighlightPolicyEntity.builder().build();
        }
        if (request.presentColor() == null
                || request.lateColor() == null
                || request.absentColor() == null
                || request.excusedColor() == null) {
            throw new IllegalArgumentException("При включённой подсветке посещаемости (enabled=true) обязательны: "
                    + "presentColor, lateColor, absentColor, excusedColor");
        }
        return AttendanceHighlightPolicyEntity.builder()
                .enabled(true)
                .presentColor(request.presentColor())
                .lateColor(request.lateColor())
                .absentColor(request.absentColor())
                .excusedColor(request.excusedColor())
                .build();
    }
}
