package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.AudienceRef;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class GroupReferenceServiceImpl implements GroupReferenceService {

    final GroupRepository groupRepository;

    final SubgroupRepository subgroupRepository;

    @Override
    public Group getGroupReferenceById(UUID id) {
        return groupRepository.getReferenceById(id);
    }

    @Override
    public Subgroup getSubgroupReferenceById(UUID id) {
        return subgroupRepository.getReferenceById(id);
    }

    @Override
    public AudienceRef resolveAudience(@Nullable UUID groupId, @Nullable UUID allowedSubgroupId) {
        if (groupId == null) {
            return new AudienceRef(null, null);
        }
        Group group = groupRepository.getReferenceById(groupId);
        Subgroup allowedSubgroup =
                allowedSubgroupId != null ? subgroupRepository.getReferenceById(allowedSubgroupId) : null;
        if (allowedSubgroup != null
                && !Objects.equals(allowedSubgroup.getGroup().getId(), group.getId())) {
            throw new IllegalArgumentException("Subgroup does not belong to the specified group");
        }
        return new AudienceRef(group, allowedSubgroup);
    }
}
