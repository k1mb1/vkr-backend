package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import java.util.List;
import java.util.UUID;

public interface SubgroupsApi {
    List<SubgroupResponse> getSubgroups(UUID groupId);
}
