package com.github.k1mb1.vkr_backend.grade.internal;

import com.github.k1mb1.vkr_backend.grade.GradeApi;
import com.github.k1mb1.vkr_backend.grade.domain.Assignment;
import com.github.k1mb1.vkr_backend.grade.domain.Grade;
import com.github.k1mb1.vkr_backend.grade.web.filters.GradeFilter;
import com.github.k1mb1.vkr_backend.grade.web.requests.UpsertGradeRequest;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeAudienceScope;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeCellResponse;
import com.github.k1mb1.vkr_backend.grade.web.responses.GradeTableResponse;
import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonRepository;
import com.github.k1mb1.vkr_backend.lesson.internal.LessonSpecifications;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import com.github.k1mb1.vkr_backend.subject.domain.TeacherSubjectPermission;
import com.github.k1mb1.vkr_backend.subject.internal.TeacherSubjectPermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class GradeService
    implements GradeApi {

    final GradeRepository gradeRepository;
    final AssignmentRepository assignmentRepository;
    final GradeMapper gradeMapper;
    final AssignmentMapper assignmentMapper;
    final TeacherSubjectPermissionRepository permissionRepository;
    final LessonRepository lessonRepository;
    final StudentRepository studentRepository;
    final LessonStudentsApi lessonStudentsApi;

    @Override
    public GradeTableResponse getGradeTable(GradeFilter filter) {
        var permission = permissionRepository.findByIdWithDetails(filter.permissionId())
            .orElseThrow(() -> new EntityNotFoundException(
                "TeacherSubjectPermission not found: " + filter.permissionId()));

        var lessons = lessonRepository.findAll(
                LessonSpecifications.forPermission(permission),
                Sort.by("startedAt"))
            .stream()
            .toList();

        var students = unionStudentsAcross(lessons);
        var audience = audienceOf(permission);

        var lessonIds = lessons.stream().map(Lesson::getId).toList();
        var studentIds = students.stream().map(Student::getId).toList();

        var assignments = lessonIds.isEmpty()
            ? List.<Assignment>of()
            : assignmentRepository.findByLessonIdsOrdered(lessonIds);

        var assignmentIds = assignments.stream().map(Assignment::getId).toList();

        var grades = studentIds.isEmpty() || assignmentIds.isEmpty()
            ? List.<Grade>of()
            : gradeRepository.findByAssignmentIdInAndStudentIdIn(assignmentIds, studentIds);

        return new GradeTableResponse(
            audience,
            students.stream().map(gradeMapper::toTableStudent).toList(),
            assignments.stream().map(assignmentMapper::toColumn).toList(),
            grades.stream().map(gradeMapper::toCell).toList()
        );
    }

    @Transactional
    @Override
    public GradeCellResponse upsert(UpsertGradeRequest request) {
        var grade = gradeRepository.findByStudentIdAndAssignmentId(
                request.studentId(),
                request.assignmentId()
            )
            .orElseGet(() -> Grade.builder()
                .student(studentRepository.getReferenceById(request.studentId()))
                .assignment(assignmentRepository.getReferenceById(request.assignmentId()))
                .build());

        grade.setValue(request.value());
        grade.setComment(request.comment());

        return gradeMapper.toCell(gradeRepository.save(grade));
    }

    private List<Student> unionStudentsAcross(List<Lesson> lessons) {
        var seen = new LinkedHashMap<UUID, Student>();
        for (var lesson : lessons) {
            for (var s : lessonStudentsApi.studentsOf(lesson)) {
                seen.putIfAbsent(s.getId(), s);
            }
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }

    private List<GradeAudienceScope> audienceOf(TeacherSubjectPermission permission) {
        if (permission.isAllPermissions()) {
            return permission.getSubject().getGroups().stream()
                .sorted(Comparator.comparing(g -> g.getName()))
                .map(g -> new GradeAudienceScope(g.getId(), g.getName(), null, null))
                .toList();
        }
        return permission.getScopes().stream()
            .sorted(Comparator
                .comparing((com.github.k1mb1.vkr_backend.subject.domain.PermissionScope s) -> s.getGroup().getName())
                .thenComparing(s -> s.getAllowedSubgroup() == null ? -1 : s.getAllowedSubgroup().getIndex()))
            .map(s -> new GradeAudienceScope(
                s.getGroup().getId(),
                s.getGroup().getName(),
                s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getId() : null,
                s.getAllowedSubgroup() != null ? s.getAllowedSubgroup().getIndex() : null
            ))
            .toList();
    }
}
