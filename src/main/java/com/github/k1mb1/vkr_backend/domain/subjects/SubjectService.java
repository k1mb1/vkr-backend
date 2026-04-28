package com.github.k1mb1.vkr_backend.domain.subjects;

import static com.github.k1mb1.vkr_backend.apis.error.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.domain.student_groups.StudentGroupRepository;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.AttachGroupToSubjectResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    final SubjectRepository subjectRepository;
    final SubjectMapper subjectMapper;
    final TeacherRepository teacherRepository;
    final StudentGroupRepository studentGroupRepository;

    public List<SubjectResponse> findAll(Specification<SubjectEntity> spec) {
        return subjectRepository
            .findAll(spec)
            .stream()
            .map(subjectMapper::toResponse)
            .toList();
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
        var entity = subjectRepository.getReferenceById(id);
        subjectMapper.update(entity, request);
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    @Transactional
    public void remove(UUID id) {
        var entity = subjectRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Subject", id)
                )
            );

        subjectRepository.delete(entity);
    }

    @Transactional
    public AttachGroupToSubjectResponse attachGroup(UUID subjectId, UUID groupId) {
        var subject = subjectRepository
            .findById(subjectId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Subject", subjectId)
                )
            );

        var group = studentGroupRepository
            .findWithSubgroupsStudentsAndSubjectsById(groupId)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Main group", groupId)
                )
            );

        Set<UUID> existingStudentIds = subject.getStudents().stream()
            .map(s -> s.getId())
            .collect(Collectors.toSet());

        group.getStudents().stream()
            .filter(s -> !existingStudentIds.contains(s.getId()))
            .forEach(subject.getStudents()::add);

        group.getSubgroups().stream()
            .flatMap(sg -> sg.getStudents().stream())
            .filter(s -> !existingStudentIds.contains(s.getId()))
            .forEach(subject.getStudents()::add);

        var savedSubject = subjectRepository.save(subject);

        return new AttachGroupToSubjectResponse(
            savedSubject.getId(),
            savedSubject.getName(),
            group.getId(),
            group.getName()
        );
    }
}
