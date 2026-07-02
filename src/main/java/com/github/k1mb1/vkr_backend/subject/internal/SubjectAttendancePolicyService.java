package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectAttendancePolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.AttendancePolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendancePolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendancePolicyResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("@authz.canAccessSubject(#subjectId)")
class SubjectAttendancePolicyService implements SubjectAttendancePolicyApi {

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Override
    public AttendancePolicyResponse getAttendancePolicy(UUID subjectId) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        return subjectMapper.toAttendancePolicyResponse(
            subject.getAttendancePolicy()
        );
    }

    @Transactional
    @Override
    public AttendancePolicyResponse updateAttendancePolicy(
        UUID subjectId,
        AttendancePolicyRequest request
    ) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        subject.setAttendancePolicy(toAttendancePolicy(request));
        return subjectMapper.toAttendancePolicyResponse(
            subjectRepository.save(subject).getAttendancePolicy()
        );
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — все поля получают
     * значения по умолчанию (через {@code @Builder.Default}).
     */
    private AttendancePolicy toAttendancePolicy(
        AttendancePolicyRequest request
    ) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return AttendancePolicy.builder().build();
        }
        if (
            request.pointsPresent() == null ||
            request.pointsLate() == null ||
            request.pointsAbsent() == null ||
            request.pointsExcused() == null
        ) {
            throw new IllegalArgumentException(
                "При включённой связке посещаемости (enabled=true) обязательны: " +
                    "pointsPresent, pointsLate, pointsAbsent, pointsExcused"
            );
        }
        return AttendancePolicy.builder()
            .enabled(true)
            .pointsPresent(request.pointsPresent())
            .pointsLate(request.pointsLate())
            .pointsAbsent(request.pointsAbsent())
            .pointsExcused(request.pointsExcused())
            .build();
    }
}
