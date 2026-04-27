package com.github.k1mb1.vkr_backend.domain.students;

import com.github.k1mb1.vkr_backend.apis.StudentApi;
import com.github.k1mb1.vkr_backend.domain.students.filters.FindStudentsFilter;
import com.github.k1mb1.vkr_backend.domain.students.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentSubjectSubgroupsResponse;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentController implements StudentApi {

    final StudentService studentService;

    @Override
    public ResponseEntity<Page<StudentResponse>> findAll(
        FindStudentsFilter filter,
        Pageable pageable
    ) {
        var studentFilter = StudentFilter.builder()
            .username(filter.username())
            .groupId(filter.groupId())
            .build();
        return ResponseEntity.ok(studentService.findAllByFilter(studentFilter, pageable));
    }

    @Override
    public ResponseEntity<StudentResponse> create(CreateStudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @Override
    public ResponseEntity<StudentResponse> update(UUID studentId, UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.update(studentId, request));
    }

    @Override
    public ResponseEntity<Void> delete(UUID studentId) {
        studentService.delete(studentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<StudentSubjectSubgroupsResponse> findSubjects(UUID id) {
        return ResponseEntity.ok(studentService.findSubjectSubgroups(id));
    }
}
