package com.github.k1mb1.vkr_backend.domain.student_groups;

import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.SubgroupResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

        var directStudents = group.getStudents().stream()
            .map(s -> s.getUsername())
            .sorted()
            .toList();

        var subgroups = group.getSubgroups().stream()
            .sorted(Comparator.comparing(StudentGroupEntity::getName))
            .map(sg -> new SubgroupResponse(
                sg.getId(),
                sg.getName(),
                sg.getStudents().stream()
                    .map(s -> s.getUsername())
                    .sorted()
                    .toList()
            ))
            .toList();

        return new StudentGroupResponse(group.getId(), group.getName(), directStudents, subgroups);
    }
}
