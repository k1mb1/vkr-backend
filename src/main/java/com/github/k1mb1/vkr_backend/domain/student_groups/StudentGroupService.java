package com.github.k1mb1.vkr_backend.domain.student_groups;

import static com.github.k1mb1.vkr_backend.apis.error.ErrorMessages.NOT_FOUND_MESSAGE;

import com.github.k1mb1.vkr_backend.domain.student_groups.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.GroupPageResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.GroupResponse;
import com.github.k1mb1.vkr_backend.domain.student_groups.responses.SubgroupResponse;
import com.github.k1mb1.vkr_backend.domain.students.StudentEntity;
import com.github.k1mb1.vkr_backend.domain.students.responses.StudentGroupMemberResponse;
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

    public Page<GroupPageResponse> findAll(
        StudentGroupFilter filter,
        Pageable pageable
    ) {
        return groupRepository
            .findAll(filter.toSpecification(), pageable)
            .map(group ->
                new GroupPageResponse(
                    group.getId(),
                    group.getName(),
                    group.getSubgroups().size()
                )
            );
    }

    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        var mainGroup = StudentGroupEntity.builder()
            .name(request.groupName())
            .build();

        var distinctIndices = request.students().stream()
            .map(StudentGroupMemberRequest::subgroupIndex)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();

        if (distinctIndices.isEmpty()) {
            for (var member : request.students()) {
                var student = StudentEntity.builder()
                    .username(member.username())
                    .group(mainGroup)
                    .build();
                mainGroup.getStudents().add(student);
            }
            return mapToGroupResponse(groupRepository.save(mainGroup));
        }

        var subgroups = new ArrayList<StudentGroupEntity>();
        for (int index : distinctIndices) {
            var sg = StudentGroupEntity.builder()
                .name(request.groupName() + "/" + (index + 1))
                .parentGroup(mainGroup)
                .build();
            subgroups.add(sg);
            mainGroup.getSubgroups().add(sg);
        }

        Map<Integer, StudentGroupEntity> indexToSubgroup = new HashMap<>();
        for (int i = 0; i < distinctIndices.size(); i++) {
            indexToSubgroup.put(distinctIndices.get(i), subgroups.get(i));
        }

        for (var member : request.students()) {
            var student = StudentEntity.builder()
                .username(member.username())
                .build();

            if (member.subgroupIndex() == null) {
                student.setGroup(mainGroup);
                mainGroup.getStudents().add(student);
            } else {
                var subgroup = indexToSubgroup.get(member.subgroupIndex());
                if (subgroup == null) {
                    throw new IllegalArgumentException(
                        "Invalid subgroupIndex: " + member.subgroupIndex()
                    );
                }
                student.setGroup(subgroup);
                subgroup.getStudents().add(student);
            }
        }

        return mapToGroupResponse(groupRepository.save(mainGroup));
    }

    public GroupResponse findGroupWithSubgroups(UUID id) {
        var group = groupRepository
            .findWithSubgroupsStudentsAndSubjectsById(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Main group", id)
                )
            );

        return mapToGroupResponse(group);
    }

    @Transactional
    public GroupResponse update(UUID groupId, UpdateGroupRequest request) {
        var group = getMainGroupById(groupId);
        group.setName(request.name());
        return mapToGroupResponse(groupRepository.save(group));
    }

    @Transactional
    public void delete(UUID groupId) {
        var group = getMainGroupById(groupId);
        groupRepository.delete(group);
    }

    private StudentGroupEntity getMainGroupById(UUID id) {
        return groupRepository
            .findByIdAndParentGroupIsNull(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    NOT_FOUND_MESSAGE.formatted("Main group", id)
                )
            );
    }

    private GroupResponse mapToGroupResponse(StudentGroupEntity entity) {
        var subgroups = entity.getSubgroups().stream()
            .sorted(Comparator.comparing(StudentGroupEntity::getName))
            .map(sg -> new SubgroupResponse(sg.getId(), sg.getName()))
            .toList();

        var students = new ArrayList<StudentGroupMemberResponse>();

        entity.getStudents().stream()
            .sorted(Comparator.comparing(StudentEntity::getUsername))
            .map(s -> new StudentGroupMemberResponse(s.getId(), s.getUsername(), null))
            .forEach(students::add);

        entity.getSubgroups().forEach(sg ->
            sg.getStudents().stream()
                .sorted(Comparator.comparing(StudentEntity::getUsername))
                .map(s -> new StudentGroupMemberResponse(s.getId(), s.getUsername(), sg.getId()))
                .forEach(students::add)
        );

        return GroupResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .subgroups(subgroups)
            .students(students)
            .build();
    }
}
