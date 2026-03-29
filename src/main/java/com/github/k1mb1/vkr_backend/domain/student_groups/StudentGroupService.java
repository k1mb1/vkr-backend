package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentEntry;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.SubgroupResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentGroupService {

    final StudentGroupRepository groupRepository;

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
            : List.of();

        return new StudentGroupResponse(group.getId(), group.getName(), directStudents, subgroups);
    }
}
