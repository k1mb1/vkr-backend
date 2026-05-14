package com.github.k1mb1.vkr_backend.group.internal;

import com.github.k1mb1.vkr_backend.group.SubgroupsApi;
import com.github.k1mb1.vkr_backend.group.web.response.SubgroupResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SubgroupService implements SubgroupsApi {

    final GroupRepository groupRepository;

    final SubgroupRepository subgroupRepository;

    final SubgroupMapper subgroupMapper;

    @Override
    public List<SubgroupResponse> getSubgroups(UUID groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new EntityNotFoundException("Group not found: " + groupId);
        }
        return subgroupRepository.findByGroupIdOrderByIndex(groupId)
            .stream()
            .map(subgroupMapper::toResponse)
            .toList();
    }
}
