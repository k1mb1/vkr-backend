package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class LessonStudentsService
    implements LessonStudentsApi {

    final StudentRepository studentRepository;

    @Override
    public List<Student> studentsOf(Lesson lesson) {
        var seen = new LinkedHashMap<UUID, Student>();
        if (lesson.isAllGroups()) {
            for (var group : lesson.getSubject().getGroups()) {
                for (var s : studentRepository.findByGroupIdAndArchivedAtIsNull(group.getId())) {
                    seen.putIfAbsent(s.getId(), s);
                }
            }
        } else {
            for (var scope : lesson.getScopes()) {
                var groupId = scope.getGroup().getId();
                var scopeStudents = scope.getAllowedSubgroup() != null
                                    ? studentRepository.findByGroupIdAndSubgroupIdAndArchivedAtIsNull(
                    groupId,
                    scope.getAllowedSubgroup().getId()
                )
                                    : studentRepository.findByGroupIdAndArchivedAtIsNull(groupId);
                for (var s : scopeStudents) {
                    seen.putIfAbsent(s.getId(), s);
                }
            }
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }
}
