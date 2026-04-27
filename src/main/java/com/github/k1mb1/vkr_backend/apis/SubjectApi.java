package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.student_grades.responses.SubjectGradesTableResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.filters.FindSubjectsFilter;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.AttachGroupToSubjectResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.FinalGradeResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping(
    value = "/api/subjects",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Subjects", description = "Subject management")
public interface SubjectApi {

    @Operation(summary = "List subjects by teacher")
    @GetMapping("/teachers/{teacherId}")
    @PreAuthorize("@securityService.isSameUser(#teacherId)")
    ResponseEntity<List<SubjectResponse>> findAllByTeacherId(
        @PathVariable UUID teacherId,
        @ParameterObject @ModelAttribute FindSubjectsFilter filter
    );

    @Operation(summary = "Create subject")
    @PostMapping
    @PreAuthorize("@securityService.isSameUser(#request.teacherId)")
    ResponseEntity<SubjectResponse> create(
        @RequestBody @Valid CreateSubjectRequest request
    );

    @Operation(summary = "Update subject metadata (name, description)")
    @PatchMapping("/{subjectId}")
    ResponseEntity<SubjectResponse> update(
        @PathVariable UUID subjectId,
        @RequestBody @Valid UpdateSubjectRequest request
    );

    @Operation(summary = "Archive subject")
    @PatchMapping("/{subjectId}/archive")
    ResponseEntity<SubjectResponse> archive(@PathVariable UUID subjectId);

    @Operation(summary = "Attach full group to subject")
    @PostMapping("/{subjectId}/groups/{groupId}")
    ResponseEntity<AttachGroupToSubjectResponse> attachGroup(
        @PathVariable UUID subjectId,
        @PathVariable UUID groupId
    );

    @Operation(summary = "Full grades table for a subject grouped by student")
    @GetMapping("/{subjectId}/grades")
    ResponseEntity<SubjectGradesTableResponse> findGrades(@PathVariable UUID subjectId);

    @Operation(
        summary = "Aggregated final grade per student for a subject",
        description = """
        For every student enrolled in the subject, computes:
        - earnedPoints: sum of (grade.value × displacementCoeff) for all graded tasks
        - maxPoints: sum of (task.maxPoints × displacementCoeff) for mandatory tasks
        - percentage: earnedPoints / maxPoints × 100 (null when maxPoints = 0)

        Displacement formula per task:
          d = lesson.issuedTaskIndex − task.position (≤ 0 → coeff = 1.0)
          NONE:     coeff = 1.0
          SUBTRACT: coeff = max(0, 1 − penaltyStep × d)
          MULTIPLY: coeff = penaltyStep ^ d
        """
    )
    @GetMapping("/{subjectId}/final-grades")
    ResponseEntity<List<FinalGradeResponse>> findFinalGrades(@PathVariable UUID subjectId);
}
