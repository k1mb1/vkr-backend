package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.apis.StudentTaskGradeApi;
import com.github.k1mb1.vkr_backend.domain.student_grades.requests.UpsertTaskGradeRequest;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentTaskGradesResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.TaskGradeResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentTaskGradeController implements StudentTaskGradeApi {

    final StudentTaskGradeService gradeService;

    @Override
    public ResponseEntity<List<StudentTaskGradesResponse>> findByLesson(UUID lessonId) {
        return ResponseEntity.ok(gradeService.findGradesByLesson(lessonId));
    }

    @Override
    public ResponseEntity<TaskGradeResponse> upsert(
        UUID lessonId,
        UUID taskId,
        UpsertTaskGradeRequest request
    ) {
        return ResponseEntity.ok(gradeService.upsert(lessonId, taskId, request));
    }
}
