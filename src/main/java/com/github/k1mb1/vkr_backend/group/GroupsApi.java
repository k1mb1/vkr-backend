package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;

public interface GroupsApi {

    GroupResponse create(CreateGroupRequest request);
}
