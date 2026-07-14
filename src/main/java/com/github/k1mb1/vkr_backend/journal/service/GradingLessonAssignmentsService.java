package com.github.k1mb1.vkr_backend.journal.service;

import com.github.k1mb1.vkr_backend.journal.service.dto.response.AssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonAssignmentResponse;
import com.github.k1mb1.vkr_backend.lesson.api.LessonAssignmentsPort;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация {@link LessonAssignmentsPort}: grading владеет заданиями и отдаёт
 * их модулю lesson в его собственной проекции (инверсия зависимости, чтобы
 * lesson не зависел от grading).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradingLessonAssignmentsService implements LessonAssignmentsPort {

    private final GradingService gradingService;

    @Override
    public List<LessonAssignmentResponse> assignmentsOfLesson(UUID lessonId) {
        return gradingService.getAssignmentsByLesson(lessonId).stream()
                .map(GradingLessonAssignmentsService::toLessonProjection)
                .toList();
    }

    @Override
    public Map<UUID, List<LessonAssignmentResponse>> assignmentsOfLessons(Collection<UUID> lessonIds) {
        return gradingService.getAssignmentsByLessons(lessonIds).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(GradingLessonAssignmentsService::toLessonProjection)
                                .toList()));
    }

    private static LessonAssignmentResponse toLessonProjection(AssignmentResponse a) {
        return new LessonAssignmentResponse(
                a.id(),
                a.lessonId(),
                a.order(),
                a.maxPoints(),
                a.required(),
                LessonAssignmentResponse.AdmissionMode.valueOf(a.admissionMode().name()),
                a.admissionMinScore(),
                a.admissionTiers() == null
                        ? null
                        : a.admissionTiers().stream()
                                .map(t -> new LessonAssignmentResponse.AdmissionTier(t.bandId(), t.minScore()))
                                .toList());
    }
}
