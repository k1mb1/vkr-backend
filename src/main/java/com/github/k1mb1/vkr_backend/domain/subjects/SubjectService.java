package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.students.StudentMapper;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    final SubjectRepository subjectRepository;
    final SubjectMapper subjectMapper;
    final TeacherRepository teacherRepository;
    final StudentRepository studentRepository;
    final StudentMapper studentMapper;

    public List<SubjectResponse> findAllByTeacherId(UUID teacherId) {
        return subjectRepository
            .findAllByTeachers_Id(teacherId)
            .stream()
            .map(subjectMapper::toResponse)
            .toList();
    }

    public SubjectEntity findEntityById(UUID id) {
        return subjectRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Subject not found: " + id)
            );
    }

    @Transactional
    public SubjectResponse create(CreateSubjectRequest request) {
        var teacher = teacherRepository.getReferenceById(request.teacherId());
        var entity = subjectMapper.toEntity(request);
        entity.getTeachers().add(teacher);
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    @Transactional
    public SubjectResponse update(UUID id, UpdateSubjectRequest request) {
        var entity = subjectRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Subject not found: " + id)
            );
        subjectMapper.update(entity, request);
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    public Page<StudentResponse> findStudentsBySubject(
        UUID subjectId,
        Pageable pageable
    ) {
        return studentRepository
            .findAllBySubjects_Id(subjectId, pageable)
            .map(studentMapper::toResponse);
    }

    @Transactional
    public void addStudentToSubject(UUID subjectId, UUID studentId) {
        var subject = subjectRepository
            .findWithStudentsById(subjectId)
            .orElseThrow(() ->
                new EntityNotFoundException("Subject not found: " + subjectId)
            );
        var student = studentRepository.getReferenceById(studentId);
        subject.getStudents().add(student);
        subjectRepository.save(subject);
    }

    @Transactional
    public void addStudentsToSubjectByUsernames(
        UUID subjectId,
        List<String> usernames
    ) {
        var subject = subjectRepository
            .findWithStudentsById(subjectId)
            .orElseThrow(() ->
                new EntityNotFoundException("Subject not found: " + subjectId)
            );

        var normalizedUsernames = usernames
            .stream()
            .map(String::trim)
            .filter(username -> !username.isBlank())
            .distinct()
            .toList();

        var existingStudentsByUsername = studentRepository
            .findAllByUsernameIn(normalizedUsernames)
            .stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    s -> s.getUsername().trim(),
                    s -> s,
                    (left, right) -> left
                )
            );

        normalizedUsernames.forEach(username -> {
            var student = existingStudentsByUsername.get(username);
            if (student == null) {
                student = studentRepository.save(
                    com.github.k1mb1.vkr_backend.domain.students.StudentEntity.builder()
                        .username(username)
                        .build()
                );
                existingStudentsByUsername.put(username, student);
            }
            subject.getStudents().add(student);
        });

        subjectRepository.save(subject);
    }

    @Transactional
    public void removeStudentFromSubject(UUID subjectId, UUID studentId) {
        var subject = subjectRepository
            .findWithStudentsById(subjectId)
            .orElseThrow(() ->
                new EntityNotFoundException("Subject not found: " + subjectId)
            );
        subject.getStudents().removeIf(s -> s.getId().equals(studentId));
        subjectRepository.save(subject);
    }
}
