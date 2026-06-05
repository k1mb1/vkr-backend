package com.github.k1mb1.vkr_backend.subject.web;

import com.github.k1mb1.vkr_backend.subject.SubjectAttendanceHighlightPolicyApi;
import com.github.k1mb1.vkr_backend.subject.web.requests.AttendanceHighlightPolicyRequest;
import com.github.k1mb1.vkr_backend.subject.web.responses.AttendanceHighlightPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance-highlight-policy/subjects/{subjectId}")
class SubjectAttendanceHighlightPolicyController {

    final SubjectAttendanceHighlightPolicyApi api;

    @GetMapping
    AttendanceHighlightPolicyResponse getAttendanceHighlightPolicy(@PathVariable UUID subjectId) {
        return api.getAttendanceHighlightPolicy(subjectId);
    }

    @PutMapping
    AttendanceHighlightPolicyResponse updateAttendanceHighlightPolicy(
        @PathVariable UUID subjectId,
        @Valid @RequestBody AttendanceHighlightPolicyRequest request
    ) {
        return api.updateAttendanceHighlightPolicy(subjectId, request);
    }
}
