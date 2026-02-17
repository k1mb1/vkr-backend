package com.github.k1mb1.vkr_backend.temp;

import com.github.k1mb1.vkr_backend.domain.teachers.TeacherMapper;
import com.github.k1mb1.vkr_backend.domain.subjects.SubjectService;
import com.github.k1mb1.vkr_backend.domain.teachers.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectTeacherService {

    private final SubjectService subjectService;
    private final TeacherService teacherService;
    private final TeacherMapper teacherMapper;

    //TODO SubjectTeacherService
//    public Page<TeacherResponse> findTeachersBySubject(UUID subjectId, Pageable pageable) {
//        var subject = subjectService.findEntityById(subjectId);
//        var teachers = subject.getTeachers();
//        var list = teachers.stream()
//                .map(teacherMapper::toResponse)
//                .toList();
//        int start = (int) pageable.getOffset();
//        int end = Math.min(start + pageable.getPageSize(), list.size());
//        return new PageImpl<>(list.subList(start, end), pageable, list.size());
//    }
//
//    @Transactional
//    public void addTeacherToSubject(UUID subjectId, UUID teacherId) {
//        var subject = subjectService.findEntityById(subjectId);
//        var teacher = teacherService.findEntityById(teacherId);
//        subject.getTeachers().add(teacher);
//        subjectService.saveEntity(subject);
//    }
//
//    @Transactional
//    public void removeTeacherFromSubject(UUID subjectId, UUID teacherId) {
//        var subject = subjectService.findEntityById(subjectId);
//        var teacher = teacherService.findEntityById(teacherId);
//        subject.getTeachers().remove(teacher);
//        subjectService.saveEntity(subject);
//    }
}