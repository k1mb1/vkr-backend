package com.github.k1mb1.vkr_backend.subject.internal;

import com.github.k1mb1.vkr_backend.common.error.ResourceNotFoundException;
import com.github.k1mb1.vkr_backend.subject.SubjectFinalAssessmentPolicyApi;
import com.github.k1mb1.vkr_backend.subject.domain.AssessmentBand;
import com.github.k1mb1.vkr_backend.subject.domain.AttendanceMode;
import com.github.k1mb1.vkr_backend.subject.domain.FinalAssessmentPolicy;
import com.github.k1mb1.vkr_backend.subject.web.requests.FinalAssessmentPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.FinalAssessmentPolicyResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@PreAuthorize("@authz.canAccessSubject(#subjectId)")
class SubjectFinalAssessmentPolicyService
    implements SubjectFinalAssessmentPolicyApi
{

    final SubjectRepository subjectRepository;

    final SubjectMapper subjectMapper;

    @PersistenceContext
    EntityManager entityManager;

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
        applyRequestToPolicy(subject.getFinalAssessmentPolicy(), request);
        return subjectMapper.toFinalAssessmentPolicyResponse(
            subjectRepository.save(subject).getFinalAssessmentPolicy()
        );
    }

    /**
     * Применяет запрос к существующей политике, обновляя её на месте.
     * Банды синхронизируются по id: существующие обновляются, новые создаются,
     * отсутствующие в запросе удаляются (orphanRemoval).
     */
    private void applyRequestToPolicy(
        FinalAssessmentPolicy policy,
        FinalAssessmentPolicyRequest request
    ) {
        if (!Boolean.TRUE.equals(request.enabled())) {
            policy.setEnabled(false);
            policy.getBands().clear();
            resetAttendance(policy);
            return;
        }
        if (request.bands() == null || request.bands().isEmpty()) {
            throw new IllegalArgumentException(
                "При включённых итогах (enabled=true) обязателен непустой bands"
            );
        }
        policy.setEnabled(true);
        syncBands(policy, request.bands());
        applyAttendanceMode(policy, request);
    }

    private void syncBands(
        FinalAssessmentPolicy policy,
        java.util.List<FinalAssessmentPolicyRequest.Band> requestBands
    ) {
        var existingById = policy
            .getBands()
            .stream()
            .collect(Collectors.toMap(AssessmentBand::getId, b -> b));

        // Какие id есть в запросе
        var requestedIds = requestBands
            .stream()
            .map(FinalAssessmentPolicyRequest.Band::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Удаляем те, которых нет в запросе
        policy.getBands().removeIf(b -> !requestedIds.contains(b.getId()));

        // Flush, чтобы DELETE старых банд ушёл в БД до INSERT новых
        entityManager.flush();

        // Обновляем существующие
        for (var req : requestBands) {
            if (req.id() != null && existingById.containsKey(req.id())) {
                var band = existingById.get(req.id());
                band.setLabel(req.label());
                band.setMinPoints(req.minPoints());
                band.setMinPercent(req.minPercent());
                band.setRequiredTasks(req.requiredTasks());
            }
        }

        // Добавляем новые
        for (var req : requestBands) {
            if (req.id() == null || !existingById.containsKey(req.id())) {
                var band = AssessmentBand.builder()
                    .policy(policy)
                    .label(req.label())
                    .minPoints(req.minPoints())
                    .minPercent(req.minPercent())
                    .requiredTasks(req.requiredTasks())
                    .build();
                policy.getBands().add(band);
            }
        }

        // Переназначаем position всем по порядку
        for (int i = 0; i < policy.getBands().size(); i++) {
            policy.getBands().get(i).setPosition(i);
        }
    }

    private void resetAttendance(FinalAssessmentPolicy policy) {
        policy.setAttendanceMode(AttendanceMode.COMBINED);
        policy.setAttendanceRequirementMode(null);
        policy.setAttendanceMinPercent(null);
        policy.setAttendanceMinCount(null);
        policy.setAttendanceCountPresent(false);
        policy.setAttendanceCountLate(false);
        policy.setAttendanceCountAbsent(false);
        policy.setAttendanceCountExcused(false);
    }

    /**
     * Применяет режим учёта посещаемости. COMBINED (по умолчанию) — посещаемость идёт в общий
     * балл через AttendancePolicy, доп. полей нет. SEPARATE — отдельный гейт: обязателен
     * attendanceRequirementMode, соответствующий порог (percent/count) и хотя бы один включённый
     * статус посещения.
     */
    private void applyAttendanceMode(
        FinalAssessmentPolicy policy,
        FinalAssessmentPolicyRequest request
    ) {
        var mode =
            request.attendanceMode() == null
                ? AttendanceMode.COMBINED
                : request.attendanceMode();
        policy.setAttendanceMode(mode);
        if (mode != AttendanceMode.SEPARATE) {
            policy.setAttendanceRequirementMode(null);
            policy.setAttendanceMinPercent(null);
            policy.setAttendanceMinCount(null);
            policy.setAttendanceCountPresent(false);
            policy.setAttendanceCountLate(false);
            policy.setAttendanceCountAbsent(false);
            policy.setAttendanceCountExcused(false);
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
                policy.setAttendanceMinPercent(request.attendanceMinPercent());
                policy.setAttendanceMinCount(null);
            }
            case COUNT -> {
                if (request.attendanceMinCount() == null) {
                    throw new IllegalArgumentException(
                        "Для COUNT обязателен attendanceMinCount"
                    );
                }
                policy.setAttendanceMinCount(request.attendanceMinCount());
                policy.setAttendanceMinPercent(null);
            }
        }
        policy.setAttendanceRequirementMode(
            request.attendanceRequirementMode()
        );
        policy.setAttendanceCountPresent(
            Boolean.TRUE.equals(request.attendanceCountPresent())
        );
        policy.setAttendanceCountLate(
            Boolean.TRUE.equals(request.attendanceCountLate())
        );
        policy.setAttendanceCountAbsent(
            Boolean.TRUE.equals(request.attendanceCountAbsent())
        );
        policy.setAttendanceCountExcused(
            Boolean.TRUE.equals(request.attendanceCountExcused())
        );
    }
}
