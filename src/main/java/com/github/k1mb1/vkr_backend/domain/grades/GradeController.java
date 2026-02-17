package com.github.k1mb1.vkr_backend.domain.grades;

import com.github.k1mb1.vkr_backend.apis.GradeApi;
import com.github.k1mb1.vkr_backend.domain.grades.requests.UpdateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.requests.CreateGradeRequest;
import com.github.k1mb1.vkr_backend.domain.grades.responses.GradeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GradeController implements GradeApi {

    private final GradeService gradeService;

    @Override
    public ResponseEntity<Page<GradeResponse>> findAll(GradeFilter filter, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(gradeService.findAll(filter, pageable));
    }

    @Override
    public ResponseEntity<GradeResponse> findById(UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(gradeService.findById(id));
    }

    @Override
    public ResponseEntity<GradeResponse> create(CreateGradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.create(request));
    }

    @Override
    public ResponseEntity<GradeResponse> update(UUID id, UpdateGradeRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(gradeService.update(id, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        gradeService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}