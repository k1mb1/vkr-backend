package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import com.github.k1mb1.vkr_backend.group.web.response.GroupWithSubgroupsResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupsApi {
    GroupResponse createGroup(CreateGroupRequest request);

    GroupResponse updateGroup(UUID id, UpdateGroupRequest request);

    GroupResponse getGroupById(UUID id);

    void deleteGroup(UUID id);

    Page<GroupPageResponse> getGroupPage(GroupFilter filter, Pageable pageable);

    List<GroupWithSubgroupsResponse> getGroupsBySubjectId(UUID subjectId);

    GroupResponse attachToSubject(UUID groupId, UUID subjectId);

    void detachFromSubject(UUID groupId, UUID subjectId);
}
