package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.web.requests.CreateGroupRequest;
import com.github.k1mb1.vkr_backend.group.web.requests.UpdateGroupRequest;
import java.util.UUID;

public interface GroupsApi {

    GroupResponse create(CreateGroupRequest request);

    GroupResponse update(UUID id, UpdateGroupRequest request);

    GroupResponse getById(UUID id);
}
