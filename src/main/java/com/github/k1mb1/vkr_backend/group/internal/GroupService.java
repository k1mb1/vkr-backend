package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.common.error.ConflictException;
import com.github.k1mb1.vkr_backend.group.GroupsApi;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupWithSubgroupsResponse;
import com.github.k1mb1.vkr_backend.student.StudentApi;
import com.github.k1mb1.vkr_backend.student.internal.StudentMapper;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.CreateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.requests.UpdateStudentRequest;
import com.github.k1mb1.vkr_backend.student.internal.web.response.StudentResponse;
import com.github.k1mb1.vkr_backend.subject.internal.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class GroupService
    implements GroupsApi {

    final GroupRepository groupRepository;

    final SubgroupRepository subgroupRepository;

    final SubjectRepository subjectRepository;

    final StudentApi studentApi;

    final SubgroupMapper subgroupMapper;

    final GroupMapper groupMapper;

    final StudentMapper studentMapper;

    @Transactional
    @Override
    public GroupResponse createGroup(CreateGroupRequest request) {
        var group = groupRepository.save(Group.builder().name(request.name()).build());

        var uniqueIndices = request.students()
            .stream()
            .map(StudentGroupMemberRequest::subgroupIndex)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        var indexToSubgroup = new HashMap<Integer, Subgroup>();
        for (var index : uniqueIndices) {
            var subgroup = subgroupRepository.save(Subgroup.builder()
                                                       .index(index)
                                                       .group(group)
                                                       .build());
            group.getSubgroups().add(subgroup);
            indexToSubgroup.put(index, subgroup);
        }

        for (var req : request.students()) {
            studentApi.createStudent(CreateStudentRequest.builder()
                                         .username(req.username())
                                         .groupId(group.getId())
                                         .subgroupId(req.subgroupIndex() != null
                                                     ? indexToSubgroup.get(req.subgroupIndex())
                                                       .getId()
                                                     : null)
                                         .build());
        }

        return toResponse(group);
    }

    @Transactional
    @Override
    public GroupResponse updateGroup(UUID id, UpdateGroupRequest request) {
        var group = groupRepository.findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));

        if (request.name() != null) {
            group.setName(request.name());
        }

        if (request.students() != null) {
            replaceRoster(group, request.students());
        }

        return toResponse(groupRepository.save(group));
    }

    private void replaceRoster(Group group, List<UpdateGroupRequest.StudentPatchRequest> roster) {
        var existingStudents = studentApi.findStudentsByGroup(group.getId());
        var existingById = existingStudents.stream()
            .collect(Collectors.toMap(StudentResponse::id, s -> s));

        var requestIds = roster.stream()
            .map(UpdateGroupRequest.StudentPatchRequest::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        for (var student : existingStudents) {
            if (!requestIds.contains(student.id())) {
                studentApi.archiveStudent(student.id());
            }
        }

        for (var req : roster) {
            if (req.id() != null) {
                var student = existingById.get(req.id());
                if (student == null || !student.groupId().equals(group.getId())) {
                    throw new jakarta.persistence.EntityNotFoundException(
                        "Student not found in group: " + req.id());
                }
                studentApi.updateStudent(
                    student.id(),
                    UpdateStudentRequest.builder()
                        .username(req.username())
                        .subgroupId(req.subgroupId())
                        .build()
                );
            } else {
                studentApi.createStudent(CreateStudentRequest.builder()
                                             .username(req.username())
                                             .groupId(group.getId())
                                             .subgroupId(req.subgroupId())
                                             .build());
            }
        }
    }

    @Override
    public GroupResponse getGroupById(UUID id) {
        var group = groupRepository.findWithDetailsById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));
        return toResponse(group);
    }

    @Transactional
    @Override
    public void deleteGroup(UUID id) {
        var group = groupRepository.findById(id)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + id));

        studentApi.deleteStudentsByGroup(group.getId());
        groupRepository.delete(group);
    }

    private GroupResponse toResponse(Group group) {
        var subgroups = group.getSubgroups().stream().map(subgroupMapper::toResponse).toList();
        var students = group.getStudents().stream().map(studentMapper::toResponse).toList();
        return groupMapper.toResponse(group, subgroups, students);
    }

    @Override
    public Page<GroupPageResponse> getGroupPage(GroupFilter filter, Pageable pageable) {
        return groupRepository.findAll(new GroupSpecifications(filter).toSpecification(), pageable)
            .map(groupMapper::toPageResponse);
    }

    @Override
    public List<GroupWithSubgroupsResponse> getGroupsBySubjectId(UUID subjectId) {
        return groupRepository.findBySubjectId(subjectId)
            .stream()
            .map(group -> groupMapper.toWithSubgroupsResponse(
                group,
                group.getSubgroups().stream().map(subgroupMapper::toResponse).toList()
            ))
            .toList();
    }

    @Transactional
    @Override
    public GroupResponse attachToSubject(UUID groupId, UUID subjectId) {
        var group = groupRepository.findById(groupId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + groupId));
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: " + subjectId));
        if (!subject.getGroups().add(group)) {
            throw new ConflictException(
                "Group " + groupId + " is already attached to subject " + subjectId);
        }
        subjectRepository.save(subject);
        return toResponse(group);
    }

    @Transactional
    @Override
    public void detachFromSubject(UUID groupId, UUID subjectId) {
        var group = groupRepository.findById(groupId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Group not found: " + groupId));
        var subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Subject not found: " + subjectId));
        if (!subject.getGroups().remove(group)) {
            throw new jakarta.persistence.EntityNotFoundException(
                "Group " + groupId + " is not attached to subject " + subjectId);
        }
        subjectRepository.save(subject);
    }
}
