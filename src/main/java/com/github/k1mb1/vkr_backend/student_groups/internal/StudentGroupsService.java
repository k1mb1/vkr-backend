package com.github.k1mb1.vkr_backend.student_groups.internal;

import com.github.k1mb1.vkr_backend.student_groups.StudentGroupsApi;
import com.github.k1mb1.vkr_backend.student_groups.domain.StudentGroup;
import com.github.k1mb1.vkr_backend.student_groups.web.filters.StudentGroupFilterRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.GroupResponse;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupListDto;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.StudentGroupMemberResponse;
import com.github.k1mb1.vkr_backend.student_groups.web.responses.SubgroupResponse;
import com.github.k1mb1.vkr_backend.students.domain.Student;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StudentGroupsService implements StudentGroupsApi {

    final StudentGroupsRepository groupRepository;
    final StudentGroupsSpecBuilder specBuilder;

    @Override
    public Page<StudentGroupListDto> findAll(
        StudentGroupFilterRequest filter,
        Pageable pageable
    ) {
        return groupRepository
            .findAll(specBuilder.build(filter), pageable)
            .map(group ->
                StudentGroupListDto.builder()
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
            groupRepository
                .findWithSubgroupsAndStudentsById(id)
                .orElseThrow(() ->
                    new EntityNotFoundException("Group not found: " + id)
                )
        );
    }

    @Override
    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        var group = StudentGroup.builder().name(request.groupName()).build();

        fillGroupFromRequest(group, request);

        return toGroupResponse(groupRepository.save(group));
    }

    @Override
    @Transactional
    public GroupResponse patch(UUID id, UpdateGroupRequest request) {
        var group = groupRepository
            .findWithSubgroupsAndStudentsById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Group not found: " + id)
            );

        group.setName(request.groupName());

        Map<UUID, StudentGroup> subgroupsById = group
            .getSubgroups()
            .stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    StudentGroup::getId,
                    subgroup -> subgroup
                )
            );
        Map<UUID, Student> existingStudentsById = collectStudentsById(group);

        group.getStudents().clear();
        group
            .getSubgroups()
            .forEach(subgroup -> subgroup.getStudents().clear());

        for (UpdateGroupRequest.StudentPatchRequest studentRequest : request.students()) {
            Student student = resolveOrCreateStudent(
                existingStudentsById,
                studentRequest
            );
            StudentGroup targetGroup = resolveTargetGroup(
                group,
                subgroupsById,
                studentRequest.subgroupId()
            );

            student.setUsername(studentRequest.username());
            student.setGroup(targetGroup);
            targetGroup.getStudents().add(student);
        }

        return toGroupResponse(group);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        StudentGroup group = groupRepository
            .findById(id)
            .orElseThrow(() ->
                new EntityNotFoundException("Group not found: " + id)
            );

        if (group.getParentGroup() != null) {
            throw new IllegalStateException("Only root group can be deleted");
        }

        groupRepository.delete(group);
    }

    private Map<UUID, Student> collectStudentsById(StudentGroup group) {
        Map<UUID, Student> studentsById = new HashMap<>();
        group
            .getStudents()
            .stream()
            .filter(student -> student.getId() != null)
            .forEach(student -> studentsById.put(student.getId(), student));
        group
            .getSubgroups()
            .forEach(subgroup ->
                subgroup
                    .getStudents()
                    .stream()
                    .filter(student -> student.getId() != null)
                    .forEach(student ->
                        studentsById.put(student.getId(), student)
                    )
            );
        return studentsById;
    }

    private Student resolveOrCreateStudent(
        Map<UUID, Student> existingStudentsById,
        UpdateGroupRequest.StudentPatchRequest studentRequest
    ) {
        if (studentRequest.id() == null) {
            return Student.builder()
                .username(studentRequest.username())
                .build();
        }

        Student student = existingStudentsById.get(studentRequest.id());
        if (Objects.isNull(student)) {
            throw new EntityNotFoundException(
                "Student not found in group: " + studentRequest.id()
            );
        }
        return student;
    }

    private StudentGroup resolveTargetGroup(
        StudentGroup group,
        Map<UUID, StudentGroup> subgroupsById,
        UUID subgroupId
    ) {
        if (subgroupId == null) {
            return group;
        }

        StudentGroup subgroup = subgroupsById.get(subgroupId);
        if (Objects.isNull(subgroup)) {
            throw new EntityNotFoundException(
                "Subgroup not found in group: " + subgroupId
            );
        }
        return subgroup;
    }

    private void fillGroupFromRequest(
        StudentGroup group,
        CreateGroupRequest request
    ) {
        Map<Integer, StudentGroup> subgroupsByIndex = new HashMap<>();

        for (CreateGroupRequest.StudentGroupMemberRequest studentRequest : request.students()) {
            Integer subgroupIndex = studentRequest.subgroupIndex();
            StudentGroup targetGroup =
                subgroupIndex == null
                    ? group
                    : subgroupsByIndex.computeIfAbsent(subgroupIndex, index ->
                          createSubgroup(group, index)
                      );

            var student = Student.builder()
                .username(studentRequest.username())
                .group(targetGroup)
                .build();
            targetGroup.getStudents().add(student);
        }
    }

    private StudentGroup createSubgroup(
        StudentGroup parentGroup,
        int subgroupIndex
    ) {
        var subgroup = StudentGroup.builder()
            .name(parentGroup.getName() + "/" + (subgroupIndex + 1))
            .parentGroup(parentGroup)
            .build();
        parentGroup.getSubgroups().add(subgroup);
        return subgroup;
    }

    private GroupResponse toGroupResponse(StudentGroup group) {
        List<StudentGroup> orderedSubgroups = group
            .getSubgroups()
            .stream()
            .sorted(Comparator.comparing(StudentGroup::getName))
            .toList();

        List<SubgroupResponse> subgroups = orderedSubgroups
            .stream()
            .map(subgroup ->
                SubgroupResponse.builder()
                    .id(subgroup.getId())
                    .name(subgroup.getName())
                    .build()
            )
            .toList();

        List<StudentGroupMemberResponse> students = new ArrayList<>();
        group
            .getStudents()
            .stream()
            .map(student ->
                StudentGroupMemberResponse.builder()
                    .id(student.getId())
                    .username(student.getUsername())
                    .build()
            )
            .forEach(students::add);
        for (StudentGroup subgroup : orderedSubgroups) {
            subgroup
                .getStudents()
                .stream()
                .map(student ->
                    StudentGroupMemberResponse.builder()
                        .id(student.getId())
                        .username(student.getUsername())
                        .subgroupId(subgroup.getId())
                        .build()
                )
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
