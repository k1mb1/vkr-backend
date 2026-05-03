package com.github.k1mb1.vkr_backend.education.subjects.internal;

import com.github.k1mb1.vkr_backend.education.subjects.api.FindSubjectsFilter;
import com.github.k1mb1.vkr_backend.education.subjects.api.SubjectApi;
import com.github.k1mb1.vkr_backend.education.subjects.api.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.education.subjects.api.responses.GroupAttachmentResponse;
import com.github.k1mb1.vkr_backend.education.subjects.api.responses.SubjectResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class SubjectController implements SubjectApi {
    final SubjectService subjectService;

    @Override
    public ResponseEntity<List<SubjectResponse>> findAllByTeacherId(UUID teacherId, FindSubjectsFilter filter) {
        return ResponseEntity.ok(subjectService.findAll(filter.toServiceFilter(teacherId)));
    }

    @Override
    public ResponseEntity<SubjectResponse> create(CreateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @Override
    public ResponseEntity<SubjectResponse> update(UUID subjectId, UpdateSubjectRequest request) {
        return ResponseEntity.ok(subjectService.update(subjectId, request));
    }

    @Override
    public ResponseEntity<GroupAttachmentResponse> attachGroup(UUID subjectId, UUID groupId) {
        return ResponseEntity.ok(subjectService.attachGroup(subjectId, groupId));
    }

    @Override
    public ResponseEntity<Void> remove(UUID subjectId) {
        subjectService.remove(subjectId);
        return ResponseEntity.noContent().build();
    }
}
