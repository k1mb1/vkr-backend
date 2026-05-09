package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.GroupReferenceService;
import com.github.k1mb1.vkr_backend.group.domain.Group;
import com.github.k1mb1.vkr_backend.group.domain.Subgroup;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
}
