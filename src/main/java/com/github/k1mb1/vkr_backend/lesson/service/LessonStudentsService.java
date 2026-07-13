package com.github.k1mb1.vkr_backend.lesson.service;

import com.github.k1mb1.vkr_backend.group.domain.StudentEntity;
import com.github.k1mb1.vkr_backend.group.repository.StudentRepository;
import com.github.k1mb1.vkr_backend.lesson.api.LessonStudentsApi;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonEntity;
import com.github.k1mb1.vkr_backend.lesson.domain.LessonScopeEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonStudentsService implements LessonStudentsApi {

    final StudentRepository studentRepository;

    @Override
    public List<StudentEntity> studentsOf(LessonEntity lesson) {
        return studentsOf(lesson.getScopes());
    }

    @Override
    public List<StudentEntity> studentsOf(LessonScopeEntity scope) {
        return studentsOf(List.of(scope));
    }

    @Override
    public List<StudentEntity> studentsOf(Collection<LessonScopeEntity> scopes) {
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

        var studentsByGroup = new HashMap<UUID, List<StudentEntity>>();
        for (var s : studentRepository.findByGroupIdInAndArchivedAtIsNull(groupIds)) {
            studentsByGroup
                    .computeIfAbsent(s.getGroup().getId(), k -> new ArrayList<>())
                    .add(s);
        }

        var seen = new LinkedHashMap<UUID, StudentEntity>();
        for (var scope : scopes) {
            collectScopeStudents(scope, studentsByGroup, seen);
        }
        var result = new ArrayList<>(seen.values());
        result.sort(Comparator.comparing(StudentEntity::getUsername));
        return result;
    }

    private void collectScopeStudents(
            LessonScopeEntity scope, Map<UUID, List<StudentEntity>> studentsByGroup, Map<UUID, StudentEntity> seen) {
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

    private static void putAll(@Nullable List<StudentEntity> students, Map<UUID, StudentEntity> seen) {
        if (students == null) {
            return;
        }
        for (var s : students) {
            seen.putIfAbsent(s.getId(), s);
        }
    }
}
