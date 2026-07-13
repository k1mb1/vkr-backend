package com.github.k1mb1.vkr_backend.group.service;

import com.github.k1mb1.vkr_backend.group.AudienceRef;
import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.GroupEntity;
import com.github.k1mb1.vkr_backend.group.domain.SubgroupEntity;
import com.github.k1mb1.vkr_backend.group.repository.GroupRepository;
import com.github.k1mb1.vkr_backend.group.repository.SubgroupRepository;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupReferenceServiceImpl implements GroupReferenceService {

    final GroupRepository groupRepository;

    final SubgroupRepository subgroupRepository;

    @Override
    public GroupEntity getGroupReferenceById(UUID id) {
        return groupRepository.getReferenceById(id);
    }

    @Override
    public SubgroupEntity getSubgroupReferenceById(UUID id) {
        return subgroupRepository.getReferenceById(id);
    }

    @Override
    public AudienceRef resolveAudience(@Nullable UUID groupId, @Nullable UUID allowedSubgroupId) {
        if (groupId == null) {
            return new AudienceRef(null, null);
        }
        GroupEntity group = groupRepository.getReferenceById(groupId);
        SubgroupEntity allowedSubgroup =
                allowedSubgroupId != null ? subgroupRepository.getReferenceById(allowedSubgroupId) : null;
        if (allowedSubgroup != null
                && !Objects.equals(allowedSubgroup.getGroup().getId(), group.getId())) {
            throw new IllegalArgumentException("Subgroup does not belong to the specified group");
        }
        return new AudienceRef(group, allowedSubgroup);
    }
}
