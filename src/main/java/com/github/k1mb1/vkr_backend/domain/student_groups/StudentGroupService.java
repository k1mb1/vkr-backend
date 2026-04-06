package com.github.k1mb1.vkr_backend.domain.student_groups;

import static com.github.k1mb1.vkr_backend.apis.error.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.StudentGroupResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.SubgroupResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentEntryResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
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

    public Page<StudentGroupPageResponse> findAll(Pageable pageable) {
        return groupRepository.findAllMainGroupsWithStudentCount(pageable);
    }

    @Transactional
    public StudentGroupResponse create(CreateGroupRequest request) {
        var mainGroup = StudentGroupEntity.builder()
            .name(request.groupName())
            .build();

        if (request.studentNames().size() > 1) {
            var orderedSubgroups = new ArrayList<StudentGroupEntity>(
                request.studentNames().size()
            );
            for (int i = 0; i < request.studentNames().size(); i++) {
                var sg = StudentGroupEntity.builder()
                    .name(request.groupName() + "/" + (i + 1))
                    .parentGroup(mainGroup)
                    .build();
                orderedSubgroups.add(sg);
                mainGroup.getSubgroups().add(sg);
            }

            for (int i = 0; i < request.studentNames().size(); i++) {
                var subgroup = orderedSubgroups.get(i);
                var studentNames = request.studentNames().get(i);

                for (String name : studentNames) {
                    var student = StudentEntity.builder()
                        .username(name)
                        .group(subgroup)
                        .build();

                    subgroup.getStudents().add(student);
                }
            }

            return mapToStudGroup(groupRepository.save(mainGroup));
        }

        for (String name : request.studentNames().getFirst()) {
            var student = StudentEntity.builder()
                .username(name)
                .group(mainGroup)
                .build();

            mainGroup.getStudents().add(student);
        }

        return mapToStudGroup(groupRepository.save(mainGroup));
    }

    public StudentGroupResponse findGroupWithSubgroups(UUID id) {
        var group = groupRepository
            .findWithSubgroupsAndStudentsById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("User", id)
                )
            );

        var subgroups = group
            .getSubgroups()
            .stream()
            .sorted(Comparator.comparing(StudentGroupEntity::getName))
            .map(sg ->
                new SubgroupResponse(
                    sg.getId(),
                    sg.getName(),
                    mapStudents(sg.getStudents())
                )
            )
            .toList();

        var directStudents = subgroups.isEmpty()
            ? mapStudents(group.getStudents())
            : List.<StudentEntryResponse>of();

        return new StudentGroupResponse(
            group.getId(),
            group.getName(),
            directStudents,
            subgroups
        );
    }

    private List<StudentEntryResponse> mapStudents(
        Collection<StudentEntity> students
    ) {
        return students
            .stream()
            .sorted(Comparator.comparing(StudentEntity::getUsername))
            .map(s -> new StudentEntryResponse(s.getId(), s.getUsername()))
            .toList();
    }

    private List<SubgroupResponse> mapSubGroups(
        Collection<StudentGroupEntity> subgroups
    ) {
        return subgroups
            .stream()
            .map(s ->
                SubgroupResponse.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .students(mapStudents(s.getStudents()))
                    .build()
            )
            .toList();
    }

    private StudentGroupResponse mapToStudGroup(StudentGroupEntity entity) {
        return StudentGroupResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .students(mapStudents(entity.getStudents()))
            .subgroups(mapSubGroups(entity.getSubgroups()))
            .build();
    }
}
