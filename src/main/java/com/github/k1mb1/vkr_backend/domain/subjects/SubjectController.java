package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.apis.SubjectApi;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SubjectController implements SubjectApi {

    private final SubjectService subjectService;

    @Override
    public ResponseEntity<Page<SubjectResponse>> findAll(SubjectFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<SubjectResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.findById(id));
    }

    @Override
    public ResponseEntity<SubjectResponse> create(CreateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @Override
    public ResponseEntity<SubjectResponse> update(UUID id, UpdateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(subjectService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        subjectService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}