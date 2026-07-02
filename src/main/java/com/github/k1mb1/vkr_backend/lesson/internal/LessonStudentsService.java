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
        return studentsOf(lesson.getScopes());
    }

    @Override
    public List<Student> studentsOf(LessonScope scope) {
        return studentsOf(List.of(scope));
    }

    @Override
    public List<Student> studentsOf(Collection<LessonScope> scopes) {
        if (scopes.isEmpty()) {
            return List.of();
        }

        // Все группы, чьи ростеры понадобятся, грузим одним запросом — вместо
        // запроса на каждый scope/группу (бывший N+1). Фильтрация по подгруппе
        // делается затем в памяти, чтобы сохранить аудиторную корректность.
        var groupIds = new HashSet<UUID>();
        for (var scope : scopes) {
            if (scope.isAllGroups()) {
                for (var group : scope.getLesson().getSubject().getGroups()) {
                    groupIds.add(group.getId());
                }
            } else if (scope.getGroup() != null) {
                groupIds.add(scope.getGroup().getId());
            }
        }
        if (groupIds.isEmpty()) {
            return List.of();
        }

        var studentsByGroup = new HashMap<UUID, List<Student>>();
        for (var s : studentRepository.findByGroupIdInAndArchivedAtIsNull(
            groupIds
        )) {
            studentsByGroup
                .computeIfAbsent(s.getGroup().getId(), k -> new ArrayList<>())
                .add(s);
        }

        var seen = new LinkedHashMap<UUID, Student>();
        for (var scope : scopes) {
            collectScopeStudents(scope, studentsByGroup, seen);
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(Student::getUsername));
        return result;
    }

    private void collectScopeStudents(
        LessonScope scope,
        Map<UUID, List<Student>> studentsByGroup,
        Map<UUID, Student> seen
    ) {
        if (scope.isAllGroups()) {
            for (var group : scope.getLesson().getSubject().getGroups()) {
                putAll(studentsByGroup.get(group.getId()), seen);
            }
            return;
        }
        if (scope.getGroup() == null) {
            return;
        }
        var groupStudents = studentsByGroup.get(scope.getGroup().getId());
        if (groupStudents == null) {
            return;
        }
        if (scope.getAllowedSubgroup() == null) {
            putAll(groupStudents, seen);
            return;
        }
        var subgroupId = scope.getAllowedSubgroup().getId();
        for (var s : groupStudents) {
            if (s.getSubgroup() != null && subgroupId.equals(s.getSubgroup().getId())) {
                seen.putIfAbsent(s.getId(), s);
            }
        }
    }

    private static void putAll(List<Student> students, Map<UUID, Student> seen) {
        if (students == null) {
            return;
        }
        for (var s : students) {
            seen.putIfAbsent(s.getId(), s);
        }
    }
}
