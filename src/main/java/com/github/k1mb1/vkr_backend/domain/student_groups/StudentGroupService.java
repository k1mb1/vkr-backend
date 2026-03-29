package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentEntry;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.SubgroupResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGroupService {

    final StudentGroupRepository groupRepository;
    final StudentRepository studentRepository;

    public StudentGroupEntity findEntityById(UUID id) {
        return groupRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Group not found: " + id)
            );
    }

    /** Paginated list of main groups with total student count. Single query. */
    public Page<StudentGroupPageResponse> findAll(Pageable pageable) {
        return groupRepository.findAllMainGroupsWithStudentCount(pageable);
    }

    /**
     * Creates a new main group with students (and optional subgroups) in one call.
     *
     * DB queries:
     *   1 SELECT  — check group name uniqueness
     *   1 SELECT  — bulk-load existing students by username
     *   1 batch INSERT — main group + subgroups
     *   1 batch INSERT — new students (if any)
     */
    @Transactional
    public StudentGroupResponse create(CreateGroupRequest request) {
        var groupName = request.groupName().trim();

        // Fail fast if name already taken
        if (groupRepository.findWithSubgroupsByNameAndParentGroupIsNull(groupName).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Group already exists: " + groupName
            );
        }

        boolean useSubgroups = request.studentNames().size() > 1;

        // 1. Create main group
        var mainGroup = groupRepository.save(
            StudentGroupEntity.builder().name(groupName).build()
        );

        // 2. Create subgroups in one batch if needed
        var subgroupByIndex = new HashMap<Integer, StudentGroupEntity>();
        if (useSubgroups) {
            var toCreate = new ArrayList<StudentGroupEntity>();
            for (int i = 0; i < request.studentNames().size(); i++) {
                var sg = StudentGroupEntity.builder()
                    .name(groupName + "/" + (i + 1))
                    .parentGroup(mainGroup)
                    .build();
                toCreate.add(sg);
            }
            var saved = groupRepository.saveAll(toCreate);
            for (int i = 0; i < saved.size(); i++) {
                subgroupByIndex.put(i, saved.get(i));
            }
        }

        // 3. One SELECT for all existing students
        var allUsernames = request.studentNames().stream()
            .flatMap(List::stream)
            .map(String::trim)
            .filter(u -> !u.isBlank())
            .distinct()
            .toList();

        var existingByUsername = new HashMap<String, StudentEntity>();
        studentRepository.findAllByUsernameIn(allUsernames)
            .forEach(s -> existingByUsername.put(s.getUsername().trim(), s));

        // 4. Build new students in memory, then one batch INSERT
        var toCreate = new ArrayList<StudentEntity>();
        for (int i = 0; i < request.studentNames().size(); i++) {
            var targetGroup = useSubgroups ? subgroupByIndex.get(i) : mainGroup;
            for (var raw : request.studentNames().get(i)) {
                var username = raw.trim();
                if (username.isBlank()) continue;
                if (!existingByUsername.containsKey(username)) {
                    var s = StudentEntity.builder()
                        .username(username)
                        .group(targetGroup)
                        .build();
                    toCreate.add(s);
                    existingByUsername.put(username, s);
                } else {
                    existingByUsername.get(username).setGroup(targetGroup);
                }
            }
        }
        if (!toCreate.isEmpty()) {
            studentRepository.saveAll(toCreate);
        }

        // 5. Build response from in-memory data — no extra SELECT
        var subgroupResponses = new ArrayList<SubgroupResponse>();
        if (useSubgroups) {
            for (int i = 0; i < request.studentNames().size(); i++) {
                var sg = subgroupByIndex.get(i);
                var students = request.studentNames().get(i).stream()
                    .map(String::trim)
                    .filter(u -> !u.isBlank())
                    .map(u -> existingByUsername.get(u))
                    .filter(s -> s != null)
                    .sorted(Comparator.comparing(StudentEntity::getUsername))
                    .map(s -> new StudentEntry(s.getId(), s.getUsername()))
                    .toList();
                subgroupResponses.add(new SubgroupResponse(sg.getId(), sg.getName(), students));
            }
        }

        var directStudents = useSubgroups
            ? List.<StudentEntry>of()
            : request.studentNames().get(0).stream()
                .map(String::trim)
                .filter(u -> !u.isBlank())
                .map(u -> existingByUsername.get(u))
                .filter(s -> s != null)
                .sorted(Comparator.comparing(StudentEntity::getUsername))
                .map(s -> new StudentEntry(s.getId(), s.getUsername()))
                .toList();

        return new StudentGroupResponse(mainGroup.getId(), groupName, directStudents, subgroupResponses);
    }

    /**
     * Returns the main group with all subgroups and their students.
     * Uses a single JOIN FETCH query — no N+1.
     */
    public StudentGroupResponse findGroupWithSubgroups(UUID id) {
        var group = groupRepository
            .findWithSubgroupsAndStudentsById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Group not found: " + id)
            );

        var subgroups = group.getSubgroups().stream()
            .sorted(Comparator.comparing(StudentGroupEntity::getName))
            .map(sg -> new SubgroupResponse(
                sg.getId(),
                sg.getName(),
                sg.getStudents().stream()
                    .sorted(Comparator.comparing(s -> s.getUsername()))
                    .map(s -> new StudentEntry(s.getId(), s.getUsername()))
                    .toList()
            ))
            .toList();

        // If subgroups exist — direct students list is empty to avoid duplication.
        // If no subgroups — all members are under the group directly.
        var directStudents = subgroups.isEmpty()
            ? group.getStudents().stream()
                .sorted(Comparator.comparing(s -> s.getUsername()))
                .map(s -> new StudentEntry(s.getId(), s.getUsername()))
                .toList()
            : List.<StudentEntry>of();

        return new StudentGroupResponse(group.getId(), group.getName(), directStudents, subgroups);
    }
}
