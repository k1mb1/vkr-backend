package com.github.k1mb1.vkr_backend.apis;

import com.github.k1mb1.vkr_backend.domain.students.responses.StudentSubjectSubgroupsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(
    value = "/api/students",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Students", description = "Student management")
public interface StudentApi {
    @Operation(
        summary = "Get subject subgroups with student names",
        description = "Returns only subgroups enrolled in the subject and names of students in each subgroup."
    )
    @GetMapping("/subjects/{subjectId}")
    ResponseEntity<StudentSubjectSubgroupsResponse> findBySubjectIdWithSubgroups(
        @PathVariable UUID subjectId
    );
}
