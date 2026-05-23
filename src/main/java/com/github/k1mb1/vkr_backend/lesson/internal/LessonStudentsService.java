package com.github.k1mb1.vkr_backend.lesson.internal;

import com.github.k1mb1.vkr_backend.lesson.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.Lesson;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScope;
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
        for (var scope : lesson.getScopes()) {
            collectScopeStudents(lesson, scope, seen);
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }

    @Override
    public List<Student> studentsOf(LessonScope scope) {
        var seen = new LinkedHashMap<UUID, Student>();
        collectScopeStudents(scope.getLesson(), scope, seen);
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }

    private void collectScopeStudents(Lesson lesson, LessonScope scope, Map<UUID, Student> seen) {
        if (scope.isAllGroups()) {
            for (var group : lesson.getSubject().getGroups()) {
                for (var s : studentRepository.findByGroupIdAndArchivedAtIsNull(group.getId())) {
                    seen.putIfAbsent(s.getId(), s);
                }
            }
            return;
        }
        if (scope.getGroup() == null) {
            return;
        }
        var groupId = scope.getGroup().getId();
        var scopeStudents = scope.getAllowedSubgroup() != null
                            ? studentRepository.findByGroupIdAndSubgroupIdAndArchivedAtIsNull(groupId,
                                                                                              scope.getAllowedSubgroup()
                                                                                              .getId()
        )
                            : studentRepository.findByGroupIdAndArchivedAtIsNull(groupId);
        for (var s : scopeStudents) {
            seen.putIfAbsent(s.getId(), s);
        }
    }
}
