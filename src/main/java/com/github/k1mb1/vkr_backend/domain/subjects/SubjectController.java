package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.apis.SubjectApi;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubjectController implements SubjectApi {

    final SubjectService subjectService;

    @Override
    public ResponseEntity<List<SubjectResponse>> findAllByTeacherId(
        UUID teacherId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            subjectService.findAllByTeacherId(teacherId)
        );
    }

    @Override
    public ResponseEntity<SubjectResponse> create(
        CreateSubjectRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            subjectService.create(request)
        );
    }

    @Override
    public ResponseEntity<List<SubjectResponse>> findAllArchivedByTeacherId(
        UUID teacherId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            subjectService.findAllArchivedByTeacherId(teacherId)
        );
    }

    @Override
    public ResponseEntity<SubjectResponse> archive(UUID subjectId) {
        return ResponseEntity.status(HttpStatus.OK).body(
            subjectService.update(
                subjectId,
                new UpdateSubjectRequest(
                    null,
                    null,
                    Boolean.TRUE,
                    Instant.now()
                )
            )
        );
    }
}
