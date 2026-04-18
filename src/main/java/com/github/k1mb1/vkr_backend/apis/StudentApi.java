package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.students.filters.FindStudentsFilter;
import com.github.k1mb1.vkr_backend.domain.students.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentSubjectSubgroupsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(
    value = "/api/students",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Students", description = "Student management")
public interface StudentApi {
    @Operation(summary = "List students")
    @GetMapping
    ResponseEntity<Page<StudentResponse>> findAll(
        @ParameterObject @ModelAttribute FindStudentsFilter filter,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "Get student by id")
    @GetMapping("/{studentId}")
    ResponseEntity<StudentResponse> findById(@PathVariable UUID studentId);

    @Operation(summary = "Update student")
    @PutMapping("/{studentId}")
    ResponseEntity<StudentResponse> update(
        @PathVariable UUID studentId,
        @RequestBody @Valid UpdateStudentRequest request
    );

    @Operation(summary = "Delete student")
    @DeleteMapping("/{studentId}")
    ResponseEntity<Void> delete(@PathVariable UUID studentId);

    @Operation(
        summary = "Get subject subgroups with student names",
        description = "Returns only subgroups enrolled in the subject and names of students in each subgroup."
    )
    @GetMapping("/subjects/{subjectId}")
    ResponseEntity<StudentSubjectSubgroupsResponse> findBySubjectIdWithSubgroups(
        @PathVariable UUID subjectId
    );
}
