package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.GroupsApi;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.student.domain.Student;
import com.github.k1mb1.vkr_backend.student.internal.StudentMapper;
import com.github.k1mb1.vkr_backend.student.internal.StudentService;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService implements GroupsApi {

    private final GroupRepository groupRepository;
    private final SubgroupRepository subgroupRepository;
    private final StudentService studentService;
    private final SubgroupMapper subgroupMapper;
    private final StudentMapper studentMapper;
    private final GroupMapper groupMapper;

    @Transactional
    @Override
    public GroupResponse create(CreateGroupRequest request) {
        var group = Group.builder().name(request.groupName()).build();
        group = groupRepository.save(group);

        var uniqueIndices = request.students().stream()
            .map(StudentGroupMemberRequest::subgroupIndex)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        var indexToSubgroup = new HashMap<Short, Subgroup>();
        for (var index : uniqueIndices) {
            var subgroup = Subgroup.builder()
                .index(index)
                .group(group)
                .build();
            subgroup = subgroupRepository.save(subgroup);
            group.getSubgroups().add(subgroup);
            indexToSubgroup.put(index, subgroup);
        }

        for (var req : request.students()) {
            studentService.create(
                req.username(),
                group,
                req.subgroupIndex() != null ? indexToSubgroup.get(req.subgroupIndex()) : null
            );
        }

        return toResponse(group);
    }

    @Transactional
    @Override
    public GroupResponse update(UUID id, UpdateGroupRequest request) {
        var group = groupRepository.findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));

        group.setName(request.groupName());

        var existingStudents = studentService.findByGroup(group);
        var existingById = existingStudents.stream()
            .collect(Collectors.toMap(Student::getId, s -> s));

        var requestIds = request.students().stream()
            .map(UpdateGroupRequest.StudentPatchRequest::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // archive removed students
        for (var student : existingStudents) {
            if (!requestIds.contains(student.getId())) {
                studentService.archive(student);
            }
        }

        // update or create
        for (var req : request.students()) {
            if (req.id() != null) {
                var student = existingById.get(req.id());
                if (student == null || !student.getGroup().getId().equals(group.getId())) {
                    throw new jakarta.persistence.EntityNotFoundException(
                        "Student not found in group: " + req.id()
                    );
                }
                var subgroup = req.subgroupId() != null
                    ? subgroupRepository.findById(req.subgroupId())
                        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                            "Subgroup not found: " + req.subgroupId()
                        ))
                    : null;
                studentService.update(student, req.username(), subgroup);
            } else {
                var subgroup = req.subgroupId() != null
                    ? subgroupRepository.findById(req.subgroupId())
                        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                            "Subgroup not found: " + req.subgroupId()
                        ))
                    : null;
                studentService.create(req.username(), group, subgroup);
            }
        }

        return toResponse(groupRepository.save(group));
    }

    @Override
    public GroupResponse getById(UUID id) {
        var group = groupRepository.findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));
        return toResponse(group);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        var group = groupRepository.findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));

        studentService.deleteByGroup(group);
        groupRepository.delete(group);
    }

    private GroupResponse toResponse(Group group) {
        var subgroups = subgroupRepository.findByGroup(group).stream()
            .map(subgroupMapper::toResponse)
            .toList();
        var students = studentService.findActiveByGroup(group).stream()
            .map(studentMapper::toResponse)
            .toList();
        return groupMapper.toResponse(group, subgroups, students);
    }

    @Override
    public Page<GroupPageResponse> getPage(GroupFilter filter, Pageable pageable) {
        return groupRepository
                .findAll(new GroupSpecifications(filter).toSpec(), pageable)
                .map(groupMapper::toPageResponse);
    }
}
