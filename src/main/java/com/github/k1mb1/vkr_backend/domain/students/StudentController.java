package com.github.k1mb1.vkr_backend.domain.students;

import com.github.k1mb1.vkr_backend.apis.StudentApi;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentSubjectSubgroupsResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentController implements StudentApi {

    final StudentService studentService;

    @Override
    public ResponseEntity<StudentSubjectSubgroupsResponse> findBySubjectIdWithSubgroups(
        UUID subjectId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
            studentService.findBySubjectIdWithSubgroups(subjectId)
        );
    }
}
