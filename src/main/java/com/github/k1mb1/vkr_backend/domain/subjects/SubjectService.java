package com.github.k1mb1.vkr_backend.domain.subjects;

import com.github.k1mb1.vkr_backend.domain.students.StudentMapper;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.CreateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.requests.UpdateSubjectRequest;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectDetailsResponse;
import com.github.k1mb1.vkr_backend.domain.subjects.responses.SubjectResponse;
import com.github.k1mb1.vkr_backend.domain.teachers.TeacherMapper;
import com.github.k1mb1.vkr_backend.domain.teachers.TeacherRepository;
import com.github.k1mb1.vkr_backend.domain.teachers.responses.TeacherResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public Page<SubjectResponse> findAll(SubjectFilter filter, Pageable pageable) {
        return subjectRepository.findAll(filter.toSpecification(), pageable)
                .map(subjectMapper::toResponse);
    }

    public List<SubjectResponse> findAllByTeacherId(UUID teacherId) {
        return subjectRepository.findAllByTeachers_Id(teacherId)
                .stream()
                .map(subjectMapper::toResponse)
                .toList();
    }

    public List<SubjectResponse> findAllByStudentId(UUID studentId) {
        return subjectRepository.findAllByStudents_Id(studentId)
                .stream()
                .map(subjectMapper::toResponse)
                .toList();
    }

    public SubjectDetailsResponse findById(UUID id) {
        return subjectRepository.findWithDetailsById(id)
                .map(subjectMapper::toDetailsResponse)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
    }

    public SubjectEntity findEntityById(UUID id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
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
        var entity = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + id));
        subjectMapper.update(entity, request);
        return subjectMapper.toResponse(subjectRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!subjectRepository.existsById(id)) {
            throw new EntityNotFoundException("Subject not found: " + id);
        }
        subjectRepository.deleteById(id);
    }

    @Transactional
    public void saveEntity(SubjectEntity entity) {
        subjectRepository.save(entity);
    }

    // --- Student association ---

    public Page<StudentResponse> findStudentsBySubject(UUID subjectId, Pageable pageable) {
        return studentRepository.findAllBySubjects_Id(subjectId, pageable)
                .map(studentMapper::toResponse);
    }

    @Transactional
    public void addStudentToSubject(UUID subjectId, UUID studentId) {
        var subject = subjectRepository.findWithStudentsById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));
        var student = studentRepository.getReferenceById(studentId);
        subject.getStudents().add(student);
        subjectRepository.save(subject);
    }

    @Transactional
    public void addStudentsToSubject(UUID subjectId, List<UUID> studentIds) {
        var subject = subjectRepository.findWithStudentsById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));
        studentIds.forEach(id -> subject.getStudents().add(studentRepository.getReferenceById(id)));
        subjectRepository.save(subject);
    }

    @Transactional
    public void removeStudentFromSubject(UUID subjectId, UUID studentId) {
        var subject = subjectRepository.findWithStudentsById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));
        subject.getStudents().removeIf(s -> s.getId().equals(studentId));
        subjectRepository.save(subject);
    }

    // --- Teacher association ---

    public Page<TeacherResponse> findTeachersBySubject(UUID subjectId, Pageable pageable) {
        return teacherRepository.findAllBySubjects_Id(subjectId, pageable)
                .map(teacherMapper::toResponse);
    }

    @Transactional
    public void addTeacherToSubject(UUID subjectId, UUID teacherId) {
        var subject = subjectRepository.findWithTeachersById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));
        var teacher = teacherRepository.getReferenceById(teacherId);
        subject.getTeachers().add(teacher);
        subjectRepository.save(subject);
    }

    @Transactional
    public void removeTeacherFromSubject(UUID subjectId, UUID teacherId) {
        var subject = subjectRepository.findWithTeachersById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + subjectId));
        subject.getTeachers().removeIf(t -> t.getId().equals(teacherId));
        subjectRepository.save(subject);
    }
}