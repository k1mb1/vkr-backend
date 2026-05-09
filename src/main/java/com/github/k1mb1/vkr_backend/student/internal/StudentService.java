package com.github.k1mb1.vkr_backend.student.internal;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public List<Student> findActiveByGroup(Group group) {
        return studentRepository.findByGroupAndArchivedAtIsNull(group);
    }

    public List<Student> findByGroup(Group group) {
        return studentRepository.findByGroup(group);
    }

    public void archive(Student student) {
        student.archive();
    }

    @Transactional
    public void update(Student student, String username, Subgroup subgroup) {
        student.setUsername(username);
        student.setSubgroup(subgroup);
        if (student.isArchived()) {
            student.unarchive();
        }
    }

    @Transactional
    public Student create(String username, Group group, Subgroup subgroup) {
        var student = Student.builder()
            .username(username)
            .group(group)
            .subgroup(subgroup)
            .build();
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteByGroup(Group group) {
        studentRepository.deleteByGroupId(group.getId());
    }
}
