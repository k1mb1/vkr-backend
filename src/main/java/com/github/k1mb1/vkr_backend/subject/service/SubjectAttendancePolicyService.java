package com.github.k1mb1.vkr_backend.subject.service;

import com.github.k1mb1.vkr_backend.common.exception.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.domain.AttendancePolicyEntity;
import com.github.k1mb1.vkr_backend.subject.mapper.SubjectMapper;
import com.github.k1mb1.vkr_backend.subject.repository.SubjectRepository;
import com.github.k1mb1.vkr_backend.subject.service.dto.request.AttendancePolicyRequest;
import com.github.k1mb1.vkr_backend.subject.service.dto.response.AttendancePolicyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectAttendancePolicyService {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @PreAuthorize("@authz.canAccessSubject(#subjectId)")
    public AttendancePolicyResponse getAttendancePolicy(UUID subjectId) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        return subjectMapper.toAttendancePolicyResponse(subject.getAttendancePolicy());
    }

    @Transactional
    @PreAuthorize("@authz.canManageSubject(#subjectId)")
    public AttendancePolicyResponse updateAttendancePolicy(UUID subjectId, AttendancePolicyRequest request) {
        var subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
        subject.setAttendancePolicy(toAttendancePolicy(request));
        return subjectMapper.toAttendancePolicyResponse(
                subjectRepository.save(subject).getAttendancePolicy());
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — все поля получают
     * значения по умолчанию (через {@code @Builder.Default}).
     */
    private AttendancePolicyEntity toAttendancePolicy(AttendancePolicyRequest request) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return AttendancePolicyEntity.builder().build();
        }
        if (request.pointsPresent() == null
                || request.pointsLate() == null
                || request.pointsAbsent() == null
                || request.pointsExcused() == null) {
            throw new IllegalArgumentException("При включённой связке посещаемости (enabled=true) обязательны: "
                    + "pointsPresent, pointsLate, pointsAbsent, pointsExcused");
        }
        return AttendancePolicyEntity.builder()
                .enabled(true)
                .pointsPresent(request.pointsPresent())
                .pointsLate(request.pointsLate())
                .pointsAbsent(request.pointsAbsent())
                .pointsExcused(request.pointsExcused())
                .build();
    }
}
