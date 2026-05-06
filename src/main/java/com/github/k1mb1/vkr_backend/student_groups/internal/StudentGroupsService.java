package com.github.k1mb1.vkr_backend.student_groups.internal;

import com.github.k1mb1.vkr_backend.student_groups.StudentGroupsApi;
import com.github.k1mb1.vkr_backend.student_groups.domain.StudentGroup;
import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.GroupResponse;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.SubgroupResponse;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupMemberResponse;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupListDto;
import com.github.k1mb1.vkr_backend.students.domain.Student;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StudentGroupsService implements StudentGroupsApi {

    final StudentGroupsRepository groupRepository;
    final StudentGroupsSpecBuilder specBuilder;

    @Override
    public Page<StudentGroupListDto> findAll(StudentGroupFilterRequest filter, Pageable pageable) {
        return groupRepository.findAll(specBuilder.build(filter), pageable)
                .map(group -> StudentGroupListDto.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .subgroupCount(group.getSubgroupCount())
                        .totalStudentCount(group.getTotalStudentCount())
                        .build()
                );
    }

    @Override
    public GroupResponse findById(UUID id) {
        return toGroupResponse(
                groupRepository.findWithSubgroupsAndStudentsById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Group not found: " + id))
        );
    }

    @Override
    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        var group = StudentGroup.builder()
                .name(request.groupName())
                .build();

        Map<Integer, StudentGroup> subgroupsByIndex = new HashMap<>();

        for (CreateGroupRequest.StudentGroupMemberRequest studentRequest : request.students()) {
            Integer subgroupIndex = studentRequest.subgroupIndex();
            StudentGroup targetGroup = subgroupIndex == null
                    ? group
                    : subgroupsByIndex.computeIfAbsent(subgroupIndex, index -> createSubgroup(group, index));

            var student = Student.builder()
                    .username(studentRequest.username())
                    .group(targetGroup)
                    .build();
            targetGroup.getStudents().add(student);
        }

        return toGroupResponse(groupRepository.save(group));
    }

    private StudentGroup createSubgroup(StudentGroup parentGroup, int subgroupIndex) {
        var subgroup = StudentGroup.builder()
                .name(parentGroup.getName() + "/" + (subgroupIndex + 1))
                .parentGroup(parentGroup)
                .build();
        parentGroup.getSubgroups().add(subgroup);
        return subgroup;
    }

    private GroupResponse toGroupResponse(StudentGroup group) {
        List<StudentGroup> orderedSubgroups = group.getSubgroups().stream()
                .sorted(Comparator.comparing(StudentGroup::getName))
                .toList();

        List<SubgroupResponse> subgroups = orderedSubgroups.stream()
                .map(subgroup -> SubgroupResponse.builder()
                        .id(subgroup.getId())
                        .name(subgroup.getName())
                        .build())
                .toList();

        List<StudentGroupMemberResponse> students = new ArrayList<>();
        group.getStudents().stream()
                .map(student -> StudentGroupMemberResponse.builder()
                        .id(student.getId())
                        .username(student.getUsername())
                        .build())
                .forEach(students::add);
        for (StudentGroup subgroup : orderedSubgroups) {
            subgroup.getStudents().stream()
                    .map(student -> StudentGroupMemberResponse.builder()
                            .id(student.getId())
                            .username(student.getUsername())
                            .subgroupId(subgroup.getId())
                            .build())
                    .forEach(students::add);
        }

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .subgroups(subgroups)
                .students(students)
                .build();
    }

}
