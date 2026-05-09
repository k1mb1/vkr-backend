package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.GroupsApi;
import com.github.k1mb1.vkr_backend.group.GroupResponse;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest.StudentGroupMemberRequest;
import com.github.k1mb1.vkr_backend.student.internal.StudentService;
import java.util.HashMap;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService implements GroupsApi {

    private final GroupRepository groupRepository;
    private final SubgroupRepository subgroupRepository;
    private final StudentService studentService;
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

        studentService.createStudentsForGroup(group, indexToSubgroup, request.students());

        return groupMapper.toResponse(group);
    }
}
