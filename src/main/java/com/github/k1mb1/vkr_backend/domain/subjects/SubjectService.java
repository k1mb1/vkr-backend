package com.github.k1mb1.vkr_backend.domain.subjects;

import static com.github.k1mb1.vkr_backend.apis.error.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupEntity;
import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
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
                    new EntityNotFoundException(NOT_FOUND_MESSAGE.formatted("Subject", id))
            );
    }

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
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Subject", id)
                )
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
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Subject", subjectId)
                )
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
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Subject", subjectId)
                )
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
                    StudentEntity.builder().username(username).build()
                );
                existingStudentsByUsername.put(username, student);
            }
            subject.getStudents().add(student);
        });

        subjectRepository.save(subject);
    }

    /**
     * Assigns students to a subject using a nested-list structure where the outer
     * index determines the subgroup.
     *
     * <ul>
     *   <li>1 inner list  → all students go to the main group, no subgroups created.</li>
     *   <li>N inner lists → subgroup "groupName/1" … "groupName/N" are created automatically.</li>
     * </ul>
     *
     * Total DB queries:
     *   1 SELECT  — subject + students (EntityGraph)
     *   1 SELECT  — main group + all its subgroups (JOIN FETCH)
     *   1 SELECT  — all existing students by username (IN clause)
     *   1 batch INSERT — new students (if any)
     *   1 batch INSERT — new subgroups (if any, via saveAll)
     *   1 INSERT/UPDATE — subject join table + student group updates (flush)
     */
    @Transactional
    public void addStudentsByGroup(
        UUID subjectId,
        AddStudentsByGroupRequest request
    ) {
        var subject = subjectRepository
            .findWithStudentsById(subjectId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Subject", subjectId)
                )
            );

        boolean useSubgroups = request.usernames().size() > 1;
        var groupName = request.groupName().trim();

        // 1. One SELECT: main group + all existing subgroups
        var mainGroup = studentGroupRepository
            .findWithSubgroupsByNameAndParentGroupIsNull(groupName)
            .orElseGet(() ->
                studentGroupRepository.save(
                    StudentGroupEntity.builder().name(groupName).build()
                )
            );

        // Build subgroup lookup map from already-loaded collection — no extra SELECTs
        var subgroupByName = mainGroup
            .getSubgroups()
            .stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    StudentGroupEntity::getName,
                    sg -> sg,
                    (a, b) -> a,
                    java.util.HashMap::new
                )
            );

        // Create missing subgroups in memory, then saveAll in one batch
        if (useSubgroups) {
            var toCreate = new java.util.ArrayList<StudentGroupEntity>();
            for (int i = 0; i < request.usernames().size(); i++) {
                var sgName = groupName + "/" + (i + 1);
                if (!subgroupByName.containsKey(sgName)) {
                    var sg = StudentGroupEntity.builder()
                        .name(sgName)
                        .parentGroup(mainGroup)
                        .build();
                    toCreate.add(sg);
                    subgroupByName.put(sgName, sg);
                }
            }
            if (!toCreate.isEmpty()) {
                studentGroupRepository.saveAll(toCreate);
            }
        }

        // 2. One SELECT: all existing students by username
        var allUsernames = request
            .usernames()
            .stream()
            .flatMap(List::stream)
            .map(String::trim)
            .filter(u -> !u.isBlank())
            .distinct()
            .toList();

        var existingByUsername = studentRepository
            .findAllByUsernameIn(allUsernames)
            .stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    s -> s.getUsername().trim(),
                    s -> s,
                    (a, b) -> a,
                    java.util.HashMap::new
                )
            );

        // Build new students in memory, then saveAll in one batch
        var toCreate = new java.util.ArrayList<
            com.github.k1mb1.vkr_backend.domain.students.StudentEntity
        >();
        for (int i = 0; i < request.usernames().size(); i++) {
            var targetGroup = useSubgroups
                ? subgroupByName.get(groupName + "/" + (i + 1))
                : mainGroup;

            for (var raw : request.usernames().get(i)) {
                var username = raw.trim();
                if (username.isBlank()) continue;

                var student = existingByUsername.get(username);
                if (student == null) {
                    student =
                        com.github.k1mb1.vkr_backend.domain.students.StudentEntity.builder()
                            .username(username)
                            .group(targetGroup)
                            .build();
                    toCreate.add(student);
                    existingByUsername.put(username, student);
                } else {
                    student.setGroup(targetGroup);
                }
            }
        }

        // 3. One batch INSERT for all new students
        if (!toCreate.isEmpty()) {
            studentRepository.saveAll(toCreate);
        }

        // 4. Add all students to subject and flush once
        existingByUsername.values().forEach(s -> subject.getStudents().add(s));
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
