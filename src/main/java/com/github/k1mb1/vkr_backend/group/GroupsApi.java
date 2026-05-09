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

    GroupResponse create(CreateGroupRequest request);

    GroupResponse update(UUID id, UpdateGroupRequest request);

    GroupResponse getById(UUID id);

    void delete(UUID id);

    Page<GroupPageResponse> getPage(GroupFilter filter, Pageable pageable);
}
