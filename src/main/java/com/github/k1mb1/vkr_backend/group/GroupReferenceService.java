package com.github.k1mb1.vkr_backend.group;

import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import java.util.UUID;

public interface GroupReferenceService {

    Group getGroupReferenceById(UUID id);

    Subgroup getSubgroupReferenceById(UUID id);
}
