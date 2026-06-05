package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectFinalAssessmentPolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.AssessmentBand;
import com.github.k1mb1.vkr_backend.subject.domain.AttendanceMode;
import com.github.k1mb1.vkr_backend.subject.domain.FinalAssessmentPolicy;
import com.github.k1mb1.vkr_backend.subject.domain.FinalAssessmentPolicy.FinalAssessmentPolicyBuilder;
import com.github.k1mb1.vkr_backend.subject.web.requests.FinalAssessmentPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.FinalAssessmentPolicyResponse;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubjectFinalAssessmentPolicyService
    implements SubjectFinalAssessmentPolicyApi
{

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @Override
    public FinalAssessmentPolicyResponse getFinalAssessmentPolicy(
        UUID subjectId
    ) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        return subjectMapper.toFinalAssessmentPolicyResponse(
            subject.getFinalAssessmentPolicy()
        );
    }

    @Transactional
    @Override
    public FinalAssessmentPolicyResponse updateFinalAssessmentPolicy(
        UUID subjectId,
        FinalAssessmentPolicyRequest request
    ) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subject", subjectId)
            );
        subject.setFinalAssessmentPolicy(toFinalAssessmentPolicy(request));
        return subjectMapper.toFinalAssessmentPolicyResponse(
            subjectRepository.save(subject).getFinalAssessmentPolicy()
        );
    }

    /**
     * Собирает новую политику из запроса. Если enabled=false — пустая политика по умолчанию.
     * Иначе обязателен непустой bands; банды сохраняются в порядке запроса (по убыванию старшинства).
     */
    private FinalAssessmentPolicy toFinalAssessmentPolicy(
        FinalAssessmentPolicyRequest request
    ) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            return FinalAssessmentPolicy.builder().build();
        }
        if (request.bands() == null || request.bands().isEmpty()) {
            throw new IllegalArgumentException(
                "При включённых итогах (enabled=true) обязателен непустой bands"
            );
        }
        var builder = FinalAssessmentPolicy.builder()
            .enabled(true)
            .bands(
                request
                    .bands()
                    .stream()
                    .map(b ->
                        AssessmentBand.builder()
                            .label(b.label())
                            .minPoints(b.minPoints())
                            .requiredTasks(b.requiredTasks())
                            .build()
                    )
                    .collect(Collectors.toCollection(ArrayList::new))
            );

        applyAttendanceMode(builder, request);
        return builder.build();
    }

    /**
     * Применяет режим учёта посещаемости. COMBINED (по умолчанию) — посещаемость идёт в общий
     * балл через AttendancePolicy, доп. полей нет. SEPARATE — отдельный гейт: обязателен
     * attendanceRequirementMode, соответствующий порог (percent/count) и хотя бы один включённый
     * статус посещения.
     */
    private void applyAttendanceMode(
        FinalAssessmentPolicyBuilder<?, ?> builder,
        FinalAssessmentPolicyRequest request
    ) {
        var mode = request.attendanceMode() == null
            ? AttendanceMode.COMBINED
            : request.attendanceMode();
        builder.attendanceMode(mode);
        if (mode != AttendanceMode.SEPARATE) {
            return;
        }
        if (request.attendanceRequirementMode() == null) {
            throw new IllegalArgumentException(
                "Для отдельного учёта посещаемости (SEPARATE) обязателен " +
                    "attendanceRequirementMode (PERCENT или COUNT)"
            );
        }
        var anyStatus =
            Boolean.TRUE.equals(request.attendanceCountPresent()) ||
            Boolean.TRUE.equals(request.attendanceCountLate()) ||
            Boolean.TRUE.equals(request.attendanceCountAbsent()) ||
            Boolean.TRUE.equals(request.attendanceCountExcused());
        if (!anyStatus) {
            throw new IllegalArgumentException(
                "Для SEPARATE включите хотя бы один статус, считающийся посещением " +
                    "(attendanceCountPresent/Late/Absent/Excused)"
            );
        }
        switch (request.attendanceRequirementMode()) {
            case PERCENT -> {
                if (request.attendanceMinPercent() == null) {
                    throw new IllegalArgumentException(
                        "Для PERCENT обязателен attendanceMinPercent (0..100)"
                    );
                }
                builder.attendanceMinPercent(request.attendanceMinPercent());
            }
            case COUNT -> {
                if (request.attendanceMinCount() == null) {
                    throw new IllegalArgumentException(
                        "Для COUNT обязателен attendanceMinCount"
                    );
                }
                builder.attendanceMinCount(request.attendanceMinCount());
            }
        }
        builder
            .attendanceRequirementMode(request.attendanceRequirementMode())
            .attendanceCountPresent(
                Boolean.TRUE.equals(request.attendanceCountPresent())
            )
            .attendanceCountLate(
                Boolean.TRUE.equals(request.attendanceCountLate())
            )
            .attendanceCountAbsent(
                Boolean.TRUE.equals(request.attendanceCountAbsent())
            )
            .attendanceCountExcused(
                Boolean.TRUE.equals(request.attendanceCountExcused())
            );
    }
}
