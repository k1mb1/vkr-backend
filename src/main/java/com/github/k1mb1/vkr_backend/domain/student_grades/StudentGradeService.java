package com.github.k1mb1.vkr_backend.domain.student_grades;

import com.github.k1mb1.vkr_backend.domain.lessons.LessonRepository;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.GradeEntryResponse;
import com.github.k1mb1.vkr_backend.domain.student_grades.responses.StudentGradesResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGradeService {
}
