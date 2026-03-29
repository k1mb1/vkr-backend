package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
import com.github.k1mb1.vkr_backend.domain.students.StudentMapper;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.AddStudentsByGroupRequest;
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
    final StudentGroupRepository studentGroupRepository;

    public List<SubjectResponse> findAllByTeacherId(UUID teacherId) {
        return subjectRepository
            .findAllByTeachers_IdAndArchivedFalse(teacherId)
            .stream()
            .map(subjectMapper::toResponse)
            .toList();
    }

    public List<SubjectResponse> findAllArchivedByTeacherId(UUID teacherId) {
        return subjectRepository
            .findAllByTeachers_IdAndArchivedTrue(teacherId)
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

    /**
     * Returns a Hibernate proxy without hitting the DB.
     * Use when you only need the entity as a FK reference (e.g. bulk inserts).
     */
    public SubjectEntity getReferenceById(UUID id) {
        return subjectRepository.getReferenceById(id);
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

    /**
     * Assigns students to a subject in bulk, auto-creating missing students,
     * the main group, and subgroups as needed.
     *
     * <p>Each entry in the request can optionally specify a subgroup name.
     * Students without a subgroup are placed directly in the main group.
     */
    @Transactional
    public void addStudentsByGroup(UUID subjectId, AddStudentsByGroupRequest request) {
        var subject = subjectRepository
            .findWithStudentsById(subjectId)
            .orElseThrow(() ->
                new EntityNotFoundException("Subject not found: " + subjectId)
            );

        // 1. Find or create the main group
        var mainGroup = studentGroupRepository
            .findByNameAndParentGroupIsNull(request.groupName().trim())
            .orElseGet(() -> studentGroupRepository.save(
                StudentGroupEntity.builder()
                    .name(request.groupName().trim())
                    .build()
            ));

        // 2. Cache subgroups by name to avoid duplicate DB calls within this request
        var subgroupCache = new java.util.HashMap<String, StudentGroupEntity>();

        // 3. Bulk-load existing students
        var normalizedUsernames = request.students().stream()
            .map(e -> e.username().trim())
            .filter(u -> !u.isBlank())
            .distinct()
            .toList();

        var existingByUsername = studentRepository
            .findAllByUsernameIn(normalizedUsernames)
            .stream()
            .collect(java.util.stream.Collectors.toMap(
                s -> s.getUsername().trim(),
                s -> s,
                (a, b) -> a
            ));

        for (var entry : request.students()) {
            var username = entry.username().trim();
            if (username.isBlank()) continue;

            // Resolve the target group (main or subgroup)
            StudentGroupEntity targetGroup;
            if (entry.subgroupName() != null && !entry.subgroupName().isBlank()) {
                var sgName = entry.subgroupName().trim();
                targetGroup = subgroupCache.computeIfAbsent(sgName, name ->
                    studentGroupRepository
                        .findByNameAndParentGroup_Id(name, mainGroup.getId())
                        .orElseGet(() -> studentGroupRepository.save(
                            StudentGroupEntity.builder()
                                .name(name)
                                .parentGroup(mainGroup)
                                .build()
                        ))
                );
            } else {
                targetGroup = mainGroup;
            }

            // Find or create the student, update their group
            var student = existingByUsername.get(username);
            if (student == null) {
                student = studentRepository.save(
                    com.github.k1mb1.vkr_backend.domain.students.StudentEntity.builder()
                        .username(username)
                        .group(targetGroup)
                        .build()
                );
                existingByUsername.put(username, student);
            } else {
                // Update group assignment if it changed
                student.setGroup(targetGroup);
            }

            subject.getStudents().add(student);
        }

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
