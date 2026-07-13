package com.github.k1mb1.vkr_backend.group.service;

import com.github.k1mb1.vkr_backend.group.api.GroupSubjectsPort;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.group.mapper.GroupMapper;
import com.github.k1mb1.vkr_backend.group.mapper.StudentMapper;
import com.github.k1mb1.vkr_backend.group.mapper.SubgroupMapper;
import com.github.k1mb1.vkr_backend.group.repository.GroupRepository;
import com.github.k1mb1.vkr_backend.group.repository.SubgroupRepository;
import com.github.k1mb1.vkr_backend.group.service.dto.filter.GroupFilter;
import com.github.k1mb1.vkr_backend.group.service.dto.request.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.request.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.GroupWithSubgroupsResponse;
import com.github.k1mb1.vkr_backend.group.service.dto.response.StudentResponse;
import com.github.k1mb1.vkr_backend.group.specification.GroupSpecifications;
import java.util.HashMap;
import java.util.List;
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
public class GroupService {

    final GroupRepository groupRepository;

    final SubgroupRepository subgroupRepository;

    final GroupSubjectsPort groupSubjectsPort;

    final StudentService studentService;

    final SubgroupMapper subgroupMapper;

    final GroupMapper groupMapper;

    final StudentMapper studentMapper;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        var group =
                groupRepository.save(GroupEntity.builder().name(request.name()).build());

        var uniqueIndices = request.students().stream()
                .map(StudentGroupMemberRequest::subgroupIndex)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        var indexToSubgroup = new HashMap<Integer, SubgroupEntity>();
        for (var index : uniqueIndices) {
            var subgroup = subgroupRepository.save(
                    SubgroupEntity.builder().index(index).group(group).build());
            group.getSubgroups().add(subgroup);
            indexToSubgroup.put(index, subgroup);
        }

        for (var req : request.students()) {
            studentService.createStudent(CreateStudentRequest.builder()
                    .username(req.username())
                    .groupId(group.getId())
                    .subgroupId(
                            req.subgroupIndex() != null
                                    ? Objects.requireNonNull(indexToSubgroup.get(req.subgroupIndex()))
                                            .getId()
                                    : null)
                    .build());
        }

        return toResponse(group);
    }

    @Transactional
    public GroupResponse updateGroup(UUID id, UpdateGroupRequest request) {
        var group = groupRepository
                .findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));

        if (request.name() != null) {
            group.setName(request.name());
        }

        if (request.students() != null) {
            replaceRoster(group, request.students());
        }

        return toResponse(groupRepository.save(group));
    }

    private void replaceRoster(GroupEntity group, List<UpdateGroupRequest.StudentPatchRequest> roster) {
        var existingStudents = studentService.findStudentsByGroup(group.getId());
        var existingById = existingStudents.stream().collect(Collectors.toMap(StudentResponse::id, s -> s));

        var requestIds = roster.stream()
                .map(UpdateGroupRequest.StudentPatchRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (var student : existingStudents) {
            if (!requestIds.contains(student.id())) {
                studentService.archiveStudent(student.id());
            }
        }

        for (var req : roster) {
            if (req.id() != null) {
                var student = existingById.get(req.id());
                if (student == null || !student.groupId().equals(group.getId())) {
                    throw new jakarta.persistence.EntityNotFoundException("Student not found in group: " + req.id());
                }
                studentService.updateStudent(
                        student.id(),
                        UpdateStudentRequest.builder()
                                .username(req.username())
                                .subgroupId(req.subgroupId())
                                .build());
            } else {
                studentService.createStudent(CreateStudentRequest.builder()
                        .username(req.username())
                        .groupId(group.getId())
                        .subgroupId(req.subgroupId())
                        .build());
            }
        }
    }

    public GroupResponse getGroupById(UUID id) {
        var group = groupRepository
                .findWithDetailsById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));
        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(UUID id) {
        var group = groupRepository
                .findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));

        studentService.deleteStudentsByGroup(group.getId());
        groupRepository.delete(group);
    }

    private GroupResponse toResponse(GroupEntity group) {
        var subgroups =
                group.getSubgroups().stream().map(subgroupMapper::toResponse).toList();
        var students =
                group.getStudents().stream().map(studentMapper::toResponse).toList();
        return groupMapper.toResponse(group, subgroups, students);
    }

    public Page<GroupPageResponse> getGroupPage(GroupFilter filter, Pageable pageable) {
        return groupRepository
                .findAll(new GroupSpecifications(filter).toSpecification(), pageable)
                .map(groupMapper::toPageResponse);
    }

    public List<GroupWithSubgroupsResponse> getGroupsBySubjectId(UUID subjectId) {
        var groupIds = groupSubjectsPort.groupIdsOfSubject(subjectId);
        return groupRepository.findWithSubgroupsByIdIn(groupIds).stream()
                .map(group -> groupMapper.toWithSubgroupsResponse(
                        group,
                        group.getSubgroups().stream()
                                .map(subgroupMapper::toResponse)
                                .toList()))
                .toList();
    }

    @Transactional
    public GroupResponse attachToSubject(UUID groupId, UUID subjectId) {
        var group = groupRepository
                .findById(groupId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + groupId));
        groupSubjectsPort.attachGroup(groupId, subjectId);
        return toResponse(group);
    }

    @Transactional
    public void detachFromSubject(UUID groupId, UUID subjectId) {
        if (!groupRepository.existsById(groupId)) {
            throw new jakarta.persistence.EntityNotFoundException("Group not found: " + groupId);
        }
        groupSubjectsPort.detachGroup(groupId, subjectId);
    }
}
