package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.web.response.GroupPageResponse;
import com.github.k1mb1.vkr_backend.group.web.filters.GroupFilter;
import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.response.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GroupsApi {

    GroupResponse createGroup(CreateGroupRequest request);

    GroupResponse updateGroup(UUID id, UpdateGroupRequest request);

    GroupResponse getGroupById(UUID id);

    void deleteGroup(UUID id);

    Page<GroupPageResponse> getGroupPage(GroupFilter filter, Pageable pageable);
}
