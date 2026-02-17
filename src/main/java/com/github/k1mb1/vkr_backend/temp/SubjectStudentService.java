package com.github.k1mb1.vkr_backend.temp;

import com.github.k1mb1.vkr_backend.domain.students.StudentMapper;
import com.github.k1mb1.vkr_backend.domain.students.StudentService;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectStudentService {

    private final SubjectService subjectService;
    private final StudentService studentService;
    private final StudentMapper studentMapper;

    //TODO SubjectStudentService
//    public Page<StudentResponse> findStudentsBySubject(UUID subjectId, Pageable pageable) {
//        var subject = subjectService.findEntityById(subjectId);
//        var students = subject.getStudents();
//        var list = students.stream()
//                .map(studentMapper::toResponse)
//                .toList();
//        int start = (int) pageable.getOffset();
//        int end = Math.min(start + pageable.getPageSize(), list.size());
//        return new PageImpl<>(list.subList(start, end), pageable, list.size());
//    }
//
//    @Transactional
//    public void addStudentToSubject(UUID subjectId, UUID studentId) {
//        var subject = subjectService.findEntityById(subjectId);
//        var student = studentService.findEntityById(studentId);
//        subject.getStudents().add(student);
//        subjectService.saveEntity(subject);
//    }
//
//    @Transactional
//    public void removeStudentFromSubject(UUID subjectId, UUID studentId) {
//        var subject = subjectService.findEntityById(subjectId);
//        var student = studentService.findEntityById(studentId);
//        subject.getStudents().remove(student);
//        subjectService.saveEntity(subject);
//    }
}